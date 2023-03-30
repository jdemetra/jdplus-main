/*
 * Copyright (c) 2018 University of Illinois at Urbana-Champaign
 * All rights reserved.

 * Developed by: 	  Matias Carrasco Kind & Sahand Hariri
 *                 NCSA/UIUC
 */
package jdplus.toolkit.base.core.ml;

import jdplus.toolkit.base.api.data.DoubleSeq;
import jdplus.toolkit.base.api.util.IntList;
import java.util.Random;
import jdplus.toolkit.base.core.data.DataBlock;
import jdplus.toolkit.base.api.math.matrices.Matrix;

/**
 *
 * @author PALATEJ
 */
@lombok.experimental.UtilityClass
public class ExtendedIsolationForest {

    IsolationForests.TreeBuilder builder(int extensionLevel) {
        return (Matrix X, int[] selection, double limit, Random rnd) -> new Builder(X, limit, extensionLevel, rnd).root(selection);
    }

    @lombok.AllArgsConstructor(access = lombok.AccessLevel.PACKAGE)
    static class XNode implements IsolationForests.Node {

        @Override
        public boolean isFinal() {
            return false;
        }

        @Override
        public int size() {
            return items.length;
        }

        @Override
        public IsolationForests.Node left() {
            return left;
        }

        @Override
        public IsolationForests.Node right() {
            return right;
        }

        final int[] items;
        final DoubleSeq normalVector;
        final double pdotn;
        final IsolationForests.Node left;
        final IsolationForests.Node right;

        @Override
        public IsolationForests.Node branch(DoubleSeq x) {
            double innerprod = normalVector.dot(x);
            if (innerprod < pdotn) {
                return left;
            } else {
                return right;
            }
        }
    }

    @lombok.AllArgsConstructor
    static class Builder {

        final Matrix X;
        final double limit;
        final int extensionLevel;
        final Random rnd;

        IsolationForests.Node root(int[] selection) {
            int[] sel = selection;
            if (sel == null) {
                sel = new int[X.getColumnsCount()];
                for (int i = 0; i < sel.length; ++i) {
                    sel[i] = i;
                }
            }
            return node(sel, 0);
        }

        IsolationForests.Node node(int[] items, int level) {
            int size = items.length;
            int dim = X.getRowsCount();

            // point, normal
            double[] xmin = new double[dim], xmax = new double[dim];
            for (int i = 0; i < dim; ++i) {
                double cmin = X.get(i, items[0]), cmax = cmin;
                for (int j = 1; j < size; ++j) {
                    double cur = X.get(i, items[j]);
                    if (cur < cmin) {
                        cmin = cur;
                    } else if (cur > cmax) {
                        cmax = cur;
                    }
                }
                xmin[i] = cmin;
                xmax[i] = cmax;
            }
            // Pick a random point on splitting hyperplane 
            // Pick a random normal vector according to specified extension level 
            IntList XL = new IntList(size), XR = new IntList(size);
            double pdotn = 0;
            int o = 0;
            int omax = 3 * dim;
            double[] p = new double[dim], n = new double[dim];
            DataBlock N = DataBlock.of(n), P = DataBlock.of(p);
            do {
                XL.clear();
                XR.clear();
                N.set(0);
                P.set(0);
                int k = extensionLevel > 0 && extensionLevel < dim ? extensionLevel : dim;
                if (k < dim) {
                    int[] idx = IsolationForests.sampleWithoutReplacement(k, dim, false, rnd);
                    for (int i = 0; i < k; ++i) {
                        n[idx[i]] = rnd.nextGaussian();
                    }
                    for (int i = 0; i < k; i++) {
                        double r = rnd.nextDouble();
                        int c = idx[i];
                        // No check for rounding issues
                        p[c] = xmin[c] + r * (xmax[c] - xmin[c]);
                    }
                } else {
                    for (int i = 0; i < dim; ++i) {
                        n[i] = rnd.nextGaussian();
                    }
                    for (int i = 0; i < dim; i++) {
                        double r = rnd.nextDouble();
                        // No check for rounding issues
                        p[i] = xmin[i] + r * (xmax[i] - xmin[i]);
                    }

                }
                // Implement splitting criterion 

                pdotn = P.dot(N);
                for (int i = 0; i < size; ++i) {
                    double innerprod = N.dot(X.column(items[i]));
                    if (innerprod < pdotn) {
                        XL.add(i);
                    } else {
                        XR.add(i);
                    }
                }
            } while (o++ < omax && (XL.isEmpty() || XR.isEmpty()));

            // Nodes with empty branches should be avoided, because they will bias
            // the length of the path. Impact on outliers detection is negligible
            // and will be ignored
            IsolationForests.Node left, right;
            if (level > limit || XL.size() <= 1) {
                left = IsolationForests.FinalNode.of(XL.size());
            } else {
                left = node(XL.toArray(), level + 1);
            }
            if (level > limit || XR.size() <= 1) {
                right = IsolationForests.FinalNode.of(XR.size());
            } else {
                right = node(XR.toArray(), level + 1);
            }
            return new XNode(items, N, pdotn, left, right);
        }
    }

}
