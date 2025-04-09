/*
 * Copyright 2021 National Bank of Belgium.
 *
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 *
 *      https://joinup.ec.europa.eu/software/page/eupl
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package jdplus.toolkit.base.api.data;

import internal.toolkit.base.api.data.InternalDoubleSeq;
import internal.toolkit.base.api.data.InternalDoubleSeqCursor;
import internal.toolkit.base.api.data.InternalDoubleVector;
import internal.toolkit.base.api.data.InternalDoubleVectorCursor;
import jdplus.toolkit.base.api.util.IntList;
import jdplus.toolkit.base.api.util.function.BiDoublePredicate;
import jdplus.toolkit.base.api.util.function.IntDoubleConsumer;
import lombok.NonNull;
import nbbrd.design.Development;
import nbbrd.design.ReturnNew;
import nbbrd.design.StaticFactoryMethod;
import org.checkerframework.checker.index.qual.NonNegative;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;
import java.util.function.*;
import java.util.stream.DoubleStream;

/**
 * Describes a sequence of doubles.
 *
 * @author Philippe Charles
 */
@Development(status = Development.Status.Release)
public interface DoubleSeq extends BaseSeq {

    interface Mutable extends DoubleSeq {

        /**
         * Sets <code>double</code> value at the specified index.
         *
         * @param index the index of the <code>double</code> value to be
         * modified
         * @param value the specified <code>double</code> value
         */
        void set(@NonNegative int index, double value) throws IndexOutOfBoundsException;

        default void set(double value) {
            DoubleSeqCursor.OnMutable cur = cursor();
            int n = length();
            for (int i = 0; i < n; ++i) {
                cur.setAndNext(value);
            }

        }

        @Override
        default DoubleSeqCursor.@NonNull OnMutable cursor() {
            return new InternalDoubleVectorCursor.DefaultDoubleVectorCursor<>(this);
        }

        @Override
        default DoubleSeq.@NonNull Mutable map(int length, @NonNull IntUnaryOperator indexMapper) {
            return onMapping(length, i -> get(indexMapper.applyAsInt(i)), (i, v) -> set(indexMapper.applyAsInt(i), v));
        }

        @Override
        default DoubleSeq.@NonNull Mutable extract(int start, int length) {
            return map(length, i -> start + i);
        }

        @Override
        default DoubleSeq.@NonNull Mutable extract(int start, int length, int increment) {
            return map(length, i -> start + i * increment);
        }

        @Override
        default DoubleSeq.Mutable drop(int beg, int end) {
            return extract(beg, length() - beg - end);
        }

        @Override
        default DoubleSeq.Mutable range(int beg, int end) {
            return end <= beg ? map(0, i -> -1) : extract(beg, end - beg);
        }

        @Override
        default DoubleSeq.Mutable reverse() {
            final int n = length();
            return map(n, i -> n - 1 - i);
        }

        @StaticFactoryMethod
        static DoubleSeq.@NonNull Mutable empty() {
            return InternalDoubleVector.MultiDoubleVector.EMPTY;
        }

        @StaticFactoryMethod
        static DoubleSeq.@NonNull Mutable of(double @NonNull [] values) {
            return new InternalDoubleVector.MultiDoubleVector(values);
        }

        @StaticFactoryMethod
        static DoubleSeq.@NonNull Mutable of(double @NonNull [] data, @NonNegative int start, @NonNegative int len, int inc) {
            return new InternalDoubleVector.MappingDoubleVector(len, i -> data[start + i * inc], (i, x) -> data[start + i * inc] = x);
        }

        @StaticFactoryMethod
        static DoubleSeq.@NonNull Mutable onMapping(@NonNegative int length, @NonNull IntToDoubleFunction getter, @NonNull IntDoubleConsumer setter) {
            return new InternalDoubleVector.MappingDoubleVector(length, getter, setter);
        }

        //<editor-fold defaultstate="collapsed" desc="Lambda expressions">
        default void apply(@NonNegative int index, DoubleUnaryOperator fn) throws IndexOutOfBoundsException {
            set(index, fn.applyAsDouble(get(index)));
        }

        default void set(DoubleSupplier fn) throws IndexOutOfBoundsException {
            DoubleSeqCursor.OnMutable cur = cursor();
            int n = length();
            for (int i = 0; i < n; ++i) {
                cur.setAndNext(fn.getAsDouble());
            }
        }

        default void set(IntToDoubleFunction fn) throws IndexOutOfBoundsException {
            DoubleSeqCursor.OnMutable cur = cursor();
            int n = length();
            for (int i = 0; i < n; ++i) {
                cur.setAndNext(fn.applyAsDouble(i));
            }
        }

        default void set(DoubleSeq z) throws IndexOutOfBoundsException {
            DoubleSeqCursor.OnMutable cur = cursor();
            DoubleSeqCursor zcur = z.cursor();
            int n = length();
            for (int i = 0; i < n; ++i) {
                cur.setAndNext(zcur.getAndNext());
            }
        }

        default void set(DoubleSeq z, DoubleUnaryOperator fn) throws IndexOutOfBoundsException {
            DoubleSeqCursor.OnMutable cur = cursor();
            DoubleSeqCursor zcur = z.cursor();
            int n = length();
            for (int i = 0; i < n; ++i) {
                cur.setAndNext(fn.applyAsDouble(zcur.getAndNext()));
            }
        }

        default void apply(DoubleUnaryOperator fn) {
            DoubleSeqCursor.OnMutable cursor = cursor();
            int n = length();
            for (int i = 0; i < n; ++i) {
                cursor.applyAndNext(fn);
            }
        }

        default void apply(DoubleSeq z, DoubleBinaryOperator fn) throws IndexOutOfBoundsException {
            DoubleSeqCursor.OnMutable cur = cursor();
            DoubleSeqCursor zcur = z.cursor();
            int n = length();
            for (int i = 0; i < n; ++i) {
                cur.applyAndNext(x -> fn.applyAsDouble(x, zcur.getAndNext()));
            }
        }

        //</editor-fold>
        //<editor-fold defaultstate="collapsed" desc="Common operations">
        default void setAY(double a, DoubleSeq y) throws IndexOutOfBoundsException {
            if (a == 1) {
                set(y);
            } else if (a == -1) {
                set(y, s -> -s);
            } else if (a != 0) {
                set(y, s -> a * s);
            }
        }

        default void add(double a) {
            if (a != 0) {
                apply(x -> x + a);
            }
        }

        default void sub(double a) {
            if (a != 0) {
                apply(x -> x - a);
            }
        }

        default void chs() {
            apply(x -> -x);
        }

        default void mul(double a) {
            if (a == 0) {
                set(0);
            } else if (a == -1) {
                chs();
            } else {
                apply(x -> x * a);
            }
        }

        default void div(double a) {
            if (a == 0) {
                set(Double.NaN);
            } else if (a == -1) {
                chs();
            } else {
                apply(x -> x * a);
            }
        }

        default void add(DoubleSeq y) throws IndexOutOfBoundsException {
            apply(y, (a, b) -> a + b);
        }

        default void addAY(double a, DoubleSeq y) throws IndexOutOfBoundsException {
            if (a == 1) {
                add(y);
            } else if (a == -1) {
                sub(y);
            } else if (a != 0) {
                apply(y, (s, t) -> s + a * t);
            }
        }

        default void sub(DoubleSeq y) throws IndexOutOfBoundsException {
            apply(y, (a, b) -> a - b);
        }

        default void mul(DoubleSeq y) throws IndexOutOfBoundsException {
            apply(y, (a, b) -> a * b);
        }

        default void div(DoubleSeq y) throws IndexOutOfBoundsException {
            apply(y, (a, b) -> a / b);
        }

        //</editor-fold>
    }

    /**
     * Returns the <code>double</code> value at the specified index. An index
     * ranges from zero to <tt>length() - 1</tt>. The first <code>double</code>
     * value of the sequence is at index zero, the next at index one, and so on,
     * as for array indexing.
     *
     * @param index the index of the <code>double</code> value to be returned
     * @return the specified <code>double</code> value
     * @throws IndexOutOfBoundsException if the <tt>index</tt> argument is
     * negative or not less than <tt>length()</tt>
     */
    double get(@NonNegative int index) throws IndexOutOfBoundsException;

    @Override
    default @NonNull
    DoubleSeqCursor cursor() {
        return new InternalDoubleSeqCursor.DefaultDoubleSeqCursor<>(this);
    }

    /**
     * Copies the data into a given buffer
     *
     * @param buffer The buffer that will receive the data.
     * @param offset The start position in the buffer for the copy. The data
     * will be copied in the buffer at the indexes [start, start+getLength()[.
     * The length of the buffer is not checked (it could be larger than this
     * array.
     */
    default void copyTo(double @NonNull [] buffer, @NonNegative int offset) {
        InternalDoubleSeq.copyToByCursor(this, buffer, offset);
    }

    /**
     * @return @see DoubleStream#toArray()
     */
    @ReturnNew
    default double @NonNull [] toArray() {
        return InternalDoubleSeq.toArrayByCursor(this);
    }

    /**
     * Returns a stream of {@code double} zero-extending the {@code double}
     * values from this sequence.
     *
     * @return an IntStream of double values from this sequence
     */
    @NonNull
    default DoubleStream stream() {
        return InternalDoubleSeq.stream(this);
    }

    default void forEach(@NonNull DoubleConsumer action) {
        InternalDoubleSeq.forEach(this, action);
    }

    /**
     * @param pred
     * @return
     * @see DoubleStream#allMatch(java.util.function.DoublePredicate))
     */
    default boolean allMatch(@NonNull DoublePredicate pred) {
        return InternalDoubleSeq.allMatchByCursor(this, pred);
    }

    /**
     * @param seq
     * @param pred
     * @return
     */
    default boolean allMatch(@NonNull DoubleSeq seq, @NonNull BiDoublePredicate pred) {
        return InternalDoubleSeq.allMatchByCursor(this, seq, pred);
    }

    /**
     * @param pred
     * @return
     * @see DoubleStream#anyMatch(java.util.function.DoublePredicate))
     */
    default boolean anyMatch(@NonNull DoublePredicate pred) {
        return InternalDoubleSeq.anyMatchByCursor(this, pred);
    }

    /**
     * @param initial
     * @param fn
     * @return
     * @see DoubleStream#reduce(double, java.util.function.DoubleBinaryOperator)
     */
    default double reduce(double initial, @NonNull DoubleBinaryOperator fn) {
        return InternalDoubleSeq.reduceByCursor(this, initial, fn);
    }

    default int indexOf(@NonNull DoublePredicate pred) {
        return InternalDoubleSeq.firstIndexOfByCursor(this, pred);
    }

    default int lastIndexOf(@NonNull DoublePredicate pred) {
        return InternalDoubleSeq.lastIndexOf(this, pred);
    }

    default int count(DoublePredicate pred) {
        return InternalDoubleSeq.countByCursor(this, pred);
    }

    @NonNull
    default DoubleSeq map(@NonNull DoubleUnaryOperator fn) {
        return onMapping(length(), i -> fn.applyAsDouble(get(i)));
    }

    @NonNull
    default DoubleSeq map(@NonNegative int length, @NonNull IntUnaryOperator indexMapper) {
        return onMapping(length, i -> get(indexMapper.applyAsInt(i)));
    }

    /**
     * Makes an extract of this data block.
     *
     * @param start The position of the first extracted item.
     * @param length The number of extracted items. The size of the result could
     * be smaller than length, if the data block doesn't contain enough items.
     * if length is -1, the max number of items is computed
     * @return A new (read only) array block. Cannot be null (but the length of
     * the result could be 0.
     */
    @NonNull
    default DoubleSeq extract(@NonNegative int start, int length) {
        if (length == -1) {
            length = length() - start;
        }
        return map(length, i -> start + i);
    }

    /**
     * Makes an extract of this data block.
     *
     * @param start The position of the first extracted item.
     * @param length The number of extracted items. The size of the result could
     * be smaller than length, if the data block doesn't contain enough items.
     * if length is -1, the max number of items is computed
     * @param increment
     * @return A new (read only) array block. Cannot be null (but the length of
     * the result could be 0.
     */
    @NonNull
    default DoubleSeq extract(@NonNegative int start, int length, int increment) {
        if (length == -1) {
            if (increment > 0) {
                length = 1 + (length() - start - 1) / increment;
            } else {
                if (start == 0) {
                    length = 1;
                } else {
                    length = 1 - start / increment;
                }
            }
        }
        return map(length, i -> start + i * increment);
    }

    /**
     * Drops some items at the beginning and/or at the end of the array
     *
     * @param beg The number of items dropped at the beginning
     * @param end The number of items dropped at the end
     * @return The shortened array
     */
    default DoubleSeq drop(int beg, int end) {
        return extract(beg, length() - beg - end);
    }

    /**
     * Range of a sequence. Other way for extracting information
     *
     * @param beg The first item
     * @param end The last item
     * @return
     */
    default DoubleSeq range(int beg, int end) {
        return end <= beg ? map(0, i -> -1) : extract(beg, end - beg);
    }

    /**
     * Removes missing values at the extremities
     *
     * @return
     */
    default DoubleSeq cleanExtremities() {
        int first = indexOf(Double::isFinite);
        if (first == -1) {
            return Doubles.EMPTY;
        }
        int last = 1 + lastIndexOf(Double::isFinite);
        if (first == 0 && last == length()) {
            return this;
        }
        return range(first, last);
    }

    /**
     * Returns a new array of doubles in reverse order
     *
     * @return
     */
    default DoubleSeq reverse() {
        final int n = length();
        return map(n, i -> n - 1 - i);
    }

    default int[] search(final DoublePredicate pred) {
        IntList list = new IntList();
        int n = length();
        DoubleSeqCursor cell = cursor();
        for (int j = 0; j < n; ++j) {
            if (pred.test(cell.getAndNext())) {
                list.add(j);
            }
        }
        return list.toArray();
    }

    default int search(final DoublePredicate pred, final int[] first) {
        int n = length();
        DoubleSeqCursor cell = cursor();
        int cur = 0;
        for (int j = 0; j < n; ++j) {
            if (pred.test(cell.getAndNext())) {
                first[cur++] = j;
                if (cur == first.length) {
                    return cur;
                }
            }
        }
        return cur;
    }

    /**
     * Computes y(t)=fn(x(t))
     *
     * @param fn The applied function
     * @return A new sequence of doubles
     */
    default DoubleSeq fn(DoubleUnaryOperator fn) {
        double[] safeArray = toArray();
        for (int i = 0; i < safeArray.length; ++i) {
            safeArray[i] = fn.applyAsDouble(safeArray[i]);
        }
        return Doubles.ofInternal(safeArray);
    }

    /**
     * Computes y(t)=fn(x(t))
     *
     * @param y
     * @param fn The applied function
     * @return A new sequence of doubles
     */
    default DoubleSeq fn(DoubleSeq y, DoubleBinaryOperator fn) {
        double[] safeArray = toArray();
        DoubleSeqCursor ycur = y.cursor();
        for (int i = 0; i < safeArray.length; ++i) {
            safeArray[i] = fn.applyAsDouble(safeArray[i], ycur.getAndNext());
        }
        return Doubles.ofInternal(safeArray);
    }

    /**
     * Computes y(t)=fn(x(t-lag), x(t))
     *
     * @param lag The lag
     * @param fn The applied function
     * @return A n-lag sequence of doubles, where n is the length of the initial
     * array.
     */
    default DoubleSeq fn(int lag, DoubleBinaryOperator fn) {
        int n = length() - lag;
        if (n <= 0) {
            return DoubleSeq.empty();
        }
        double[] safeArray = new double[n];
        if (lag == 1) {
            DoubleSeqCursor cursor = cursor();
            double prev = cursor.getAndNext();
            for (int i = 0; i < n; ++i) {
                double next = cursor.getAndNext();
                safeArray[i] = fn.applyAsDouble(prev, next);
                prev = next;
            }
        } else {
            for (int j = 0; j < lag; ++j) {
                double prev = get(j);
                for (int i = j; i < n; i += lag) {
                    double next = get(i + lag);
                    safeArray[i] = fn.applyAsDouble(prev, next);
                    prev = next;
                }
            }
        }
        return Doubles.ofInternal(safeArray);
    }

    default DoubleSeq extend(@NonNegative int nbeg, @NonNegative int nend) {
        int n = length() + nbeg + nend;
        double[] safeArray = new double[n];
        for (int i = 0; i < nbeg; ++i) {
            safeArray[i] = Double.NaN;
        }
        copyTo(safeArray, nbeg);
        for (int i = n - nend; i < n; ++i) {
            safeArray[i] = Double.NaN;
        }
        return Doubles.ofInternal(safeArray);
    }

    default DoubleSeq select(DoublePredicate pred) {
        double[] x = toArray();
        int cur = 0;
        for (int i = 0; i < x.length; ++i) {
            if (pred.test(x[i])) {
                if (cur < i) {
                    x[cur] = x[i];
                }
                ++cur;
            }
        }
        if (cur == x.length) {
            return Doubles.ofInternal(x);
        } else {
            double[] xc = new double[cur];
            System.arraycopy(x, 0, xc, 0, cur);
            return Doubles.ofInternal(xc);
        }
    }

    default DoubleSeq select(IntList selection) {
        if (selection == null) {
            return this;
        }
        final int[] sel = selection.toArray();
        return Doubles.of(sel.length, i -> get(sel[i]));
    }

    default DoubleSeq op(DoubleSeq b, DoubleBinaryOperator op) {
        double[] safeArray = toArray();
        DoubleSeqCursor cursor = b.cursor();
        for (int i = 0; i < safeArray.length; ++i) {
            safeArray[i] = op.applyAsDouble(safeArray[i], cursor.getAndNext());
        }
        return Doubles.ofInternal(safeArray);
    }

    default DoubleSeq plus(double del) {
        if (del == 0) {
            return this;
        }
        double[] safeArray = toArray();
        for (int i = 0; i < safeArray.length; ++i) {
            safeArray[i] += del;
        }
        return Doubles.ofInternal(safeArray);
    }

    default DoubleSeq times(double factor) {
        if (factor == 1) {
            return this;
        }
        if (factor == 0) {
            return onMapping(this.length(), i -> 0);
        }
        double[] safeArray = toArray();
        if (factor == -1) {
            for (int i = 0; i < safeArray.length; ++i) {
                safeArray[i] = -safeArray[i];
            }
        } else {
            for (int i = 0; i < safeArray.length; ++i) {
                safeArray[i] *= factor;
            }
        }
        return Doubles.ofInternal(safeArray);
    }

    default DoubleSeq fastOp(DoubleUnaryOperator op) {
        int n = length();
        return onMapping(n, i -> op.applyAsDouble(get(i)));
    }

    default DoubleSeq fastOp(DoubleSeq b, DoubleBinaryOperator op) {
        int n = length();
        return onMapping(n, i -> op.applyAsDouble(get(i), b.get(i)));
    }

    default DoubleSeq commit() {
        return Doubles.ofInternal(this.toArray());
    }

    default double sum() {
        return DoublesMath.sum(this);
    }

    default double average() {
        return DoublesMath.average(this);
    }

    default double ssq() {
        return DoublesMath.ssq(this);
    }

    default double ssqc(double mean) {
        return DoublesMath.ssqc(this, mean);
    }

    default double sumWithMissing() {
        return DoublesMath.sumWithMissing(this);
    }

    default double ssqWithMissing() {
        return DoublesMath.ssqWithMissing(this);
    }

    default double ssqcWithMissing(final double mean) {
        return DoublesMath.ssqcWithMissing(this, mean);
    }

    default double averageWithMissing() {
        return DoublesMath.averageWithMissing(this);
    }

    public default double norm1() {
        return DoublesMath.norm1(this);
    }

    /**
     * Computes the euclidian norm of the src block. Based on the "dnrm2" Lapack
     * function.
     *
     * @return The euclidian norm (&gt=0).
     */
    default double norm2() {
        return DoublesMath.norm2(this);
    }

    default double fastNorm2() {
        return DoublesMath.fastNorm2(this);
    }

    /**
     * Computes the infinite-norm of this src block
     *
     * @return Returns min{|src(i)|}
     */
    default double normInf() {
        return DoublesMath.normInf(this);
    }

    default double max() {
        int n = length();
        DoubleSeqCursor cursor = this.cursor();
        double max = Double.NaN;
        for (int i = 0; i < n; ++i) {
            double x = cursor.getAndNext();
            if (!Double.isNaN(x)) {
                if (Double.isNaN(max)) {
                    max = x;
                } else if (x > max) {
                    max = x;
                }
            }
        }
        return max;
    }

    default double min() {
        int n = length();
        DoubleSeqCursor cursor = this.cursor();
        double min = Double.NaN;
        for (int i = 0; i < n; ++i) {
            double x = cursor.getAndNext();
            if (!Double.isNaN(x)) {
                if (Double.isNaN(min)) {
                    min = x;
                } else if (x < min) {
                    min = x;
                }
            }
        }
        return min;
    }

    default Interval range() {
        int n = length();
        DoubleSeqCursor cursor = this.cursor();
        double min = Double.NaN, max = Double.NaN;
        for (int i = 0; i < n; ++i) {
            double x = cursor.getAndNext();
            if (!Double.isNaN(x)) {
                if (Double.isNaN(min)) {
                    min = max = x;
                } else if (x < min) {
                    min = x;
                } else if (x > max) {
                    max = x;
                }
            }
        }
        return new Interval(min, max);
    }

    /**
     * Counts the number of identical consecutive values.
     *
     * @return Missing values are omitted.
     */
    default int getRepeatCount() {
        return DoublesMath.getRepeatCount(this);
    }

    default double dot(DoubleSeq data) {
        return DoublesMath.dot(this, data);
    }

    default double jdot(DoubleSeq data, int pos) {
        return DoublesMath.jdot(this, data, pos);
    }

    default double distance(DoubleSeq data) {
        return DoublesMath.distance(this, data);
    }

    default DoubleSeq removeMean() {
        return DoublesMath.removeMean(this);
    }

    default DoubleSeq delta(int lag) {
        return DoublesMath.delta(this, lag);
    }

    default DoubleSeq delta(int lag, int pow) {
        return DoublesMath.delta(this, lag, pow);
    }

    default DoubleSeq log() {
        return fn(Math::log);
    }

    default DoubleSeq exp() {
        return fn(Math::exp);
    }

    default DoubleSeq sqrt() {
        return fn(Math::sqrt);
    }

    default boolean hasSameContentAs(DoubleSeq that) {
        return InternalDoubleSeq.hasSameContentAs(this, that);
    }

    static int getHashCode(DoubleSeq values) {
        int result = 1;
        for (int i = 0; i < values.length(); i++) {
            long bits = Double.doubleToLongBits(values.get(i));
            result = 31 * result + (int) (bits ^ (bits >>> 32));
        }
        return result;
    }

    static boolean equals(double a, double b, double epsilon) {
        return a > b ? (a - epsilon <= b) : (b - epsilon <= a);
    }

    static String format(DoubleSeq rd) {
        StringBuilder builder = new StringBuilder();
        int n = rd.length();
        if (n > 0) {
            builder.append(rd.get(0));
            for (int i = 1; i < n; ++i) {
                builder.append('\t').append(rd.get(i));
            }
        }
        return builder.toString();
    }

    static String format(DoubleSeq rd, String fmt) {
        StringBuilder builder = new StringBuilder();
        int n = rd.length();
        if (n > 0) {
            if (fmt != null) {
                DecimalFormat df;
                df = new DecimalFormat(fmt, DecimalFormatSymbols.getInstance(Locale.ROOT));
                DoubleSeqCursor cursor = rd.cursor();
                builder.append(df.format(cursor.getAndNext()));
                for (int i = 1; i < n; ++i) {
                    builder.append('\t').append(df.format(cursor.getAndNext()));

                }
            } else {
                builder.append(rd.get(0));
                for (int i = 1; i < n; ++i) {
                    builder.append('\t').append(rd.get(i));
                }
            }
        }
        return builder.toString();
    }

    static String format(DoubleSeq rd, DecimalFormat fmt) {
        StringBuilder builder = new StringBuilder();
        int n = rd.length();
        if (n > 0) {
            DoubleSeqCursor cursor = rd.cursor();
            builder.append(fmt.format(cursor.getAndNext()));
            for (int i = 1; i < n; ++i) {
                builder.append('\t').append(fmt.format(cursor.getAndNext()));
            }
        }
        return builder.toString();
    }

    static double round(double r, final int ndec) {
        if (ndec < 0) {
            throw new IllegalArgumentException("Negative rounding parameter");
        }
        double f = 1;
        for (int i = 0; i < ndec; ++i) {
            f *= 10;
        }
        if (Double.isFinite(r)) {
            double v = r;
            if (ndec > 0) {
                r = Math.round(v * f) / f;
            } else {
                r = Math.round(v);
            }
        }
        return r;
    }

    //<editor-fold defaultstate="collapsed" desc="Factories">
    double[] EMPTYARRAY = new double[0];

    /**
     * Creates a new value using an array of doubles.
     *
     * @param data
     * @return
     */
    @StaticFactoryMethod
    @NonNull
    static DoubleSeq of(double @NonNull ... data) {
        return Doubles.ofInternal(data);
    }

    /**
     * @param data Storage
     * @param start Position of the first item (non negative)
     * @param len Number of items (non negative)
     * @return
     */
    @StaticFactoryMethod
    @NonNull
    static DoubleSeq of(double @NonNull [] data, @NonNegative int start, @NonNegative int len) {
        return new InternalDoubleSeq.SubDoubleSeq(data, start, len);
    }

    /**
     * Makes a sequence of regularly spaced doubles
     *
     * @param data Storage
     * @param start Position of the first item (non negative)
     * @param len Number of items (non negative)
     * @param inc Increment in the underlying storage of two succesive items
     * @return
     */
    @StaticFactoryMethod
    @NonNull
    static DoubleSeq of(double @NonNull [] data, @NonNegative int start, @NonNegative int len, int inc) {
        return new InternalDoubleSeq.RegularlySpacedDoubles(data, start, len, inc);
    }

    @StaticFactoryMethod
    @NonNull
    static DoubleSeq empty() {
        return Doubles.EMPTY;
    }

    @StaticFactoryMethod
    @NonNull
    static DoubleSeq zero() {
        return Doubles.ZERO;
    }

    @StaticFactoryMethod
    @NonNull
    static DoubleSeq one() {
        return Doubles.ONE;
    }

    @StaticFactoryMethod
    @NonNull
    static DoubleSeq onMapping(@NonNegative int length, @NonNull IntToDoubleFunction getter) {
        return new InternalDoubleSeq.MappingDoubleSeq(length, getter);
    }

    @StaticFactoryMethod
    @NonNull
    static DoubleSeq pooled(@NonNull DoubleSeq[] seqs) {
        // TODO improve the current solution (without necessarly copying the data)
        if (seqs.length == 0) {
            return Doubles.EMPTY;
        }
        if (seqs.length == 1) {
            return seqs[0];
        }
        int csize = 0;
        for (int i = 0; i < seqs.length; ++i) {
            csize += seqs[i].length();
        }
        double[] all = new double[csize];
        int pos = 0;
        for (int i = 0; i < seqs.length; ++i) {
            seqs[i].copyTo(all, pos);
            pos += seqs[i].length();
        }
        return DoubleSeq.of(all);
    }
    //</editor-fold>
}
