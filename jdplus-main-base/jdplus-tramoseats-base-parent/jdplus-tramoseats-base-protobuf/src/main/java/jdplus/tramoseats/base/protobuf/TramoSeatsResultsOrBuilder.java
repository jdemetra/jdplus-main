// Generated by the protocol buffer compiler.  DO NOT EDIT!
// NO CHECKED-IN PROTOBUF GENCODE
// source: tramoseats.proto
// Protobuf Java Version: 4.30.2

package jdplus.tramoseats.base.protobuf;

public interface TramoSeatsResultsOrBuilder extends
    // @@protoc_insertion_point(interface_extends:tramoseats.TramoSeatsResults)
    com.google.protobuf.MessageOrBuilder {

  /**
   * <code>.regarima.RegArimaModel preprocessing = 1;</code>
   * @return Whether the preprocessing field is set.
   */
  boolean hasPreprocessing();
  /**
   * <code>.regarima.RegArimaModel preprocessing = 1;</code>
   * @return The preprocessing.
   */
  jdplus.toolkit.base.protobuf.regarima.RegArimaProtos.RegArimaModel getPreprocessing();
  /**
   * <code>.regarima.RegArimaModel preprocessing = 1;</code>
   */
  jdplus.toolkit.base.protobuf.regarima.RegArimaProtos.RegArimaModelOrBuilder getPreprocessingOrBuilder();

  /**
   * <code>.tramoseats.SeatsResults decomposition = 2;</code>
   * @return Whether the decomposition field is set.
   */
  boolean hasDecomposition();
  /**
   * <code>.tramoseats.SeatsResults decomposition = 2;</code>
   * @return The decomposition.
   */
  jdplus.tramoseats.base.protobuf.SeatsResults getDecomposition();
  /**
   * <code>.tramoseats.SeatsResults decomposition = 2;</code>
   */
  jdplus.tramoseats.base.protobuf.SeatsResultsOrBuilder getDecompositionOrBuilder();

  /**
   * <code>.sa.SaDecomposition final = 3;</code>
   * @return Whether the final field is set.
   */
  boolean hasFinal();
  /**
   * <code>.sa.SaDecomposition final = 3;</code>
   * @return The final.
   */
  jdplus.sa.base.protobuf.SaProtos.SaDecomposition getFinal();
  /**
   * <code>.sa.SaDecomposition final = 3;</code>
   */
  jdplus.sa.base.protobuf.SaProtos.SaDecompositionOrBuilder getFinalOrBuilder();

  /**
   * <code>.sa.Diagnostics diagnostics_sa = 5;</code>
   * @return Whether the diagnosticsSa field is set.
   */
  boolean hasDiagnosticsSa();
  /**
   * <code>.sa.Diagnostics diagnostics_sa = 5;</code>
   * @return The diagnosticsSa.
   */
  jdplus.sa.base.protobuf.SaProtos.Diagnostics getDiagnosticsSa();
  /**
   * <code>.sa.Diagnostics diagnostics_sa = 5;</code>
   */
  jdplus.sa.base.protobuf.SaProtos.DiagnosticsOrBuilder getDiagnosticsSaOrBuilder();
}
