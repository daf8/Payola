package cz.payola.common.entities

import scala.collection._
import cz.payola.common.Entity
import cz.payola.common.entities.plugins._
import cz.payola.common.entities.pipelines._

trait Pipeline extends Entity with NamedEntity with OptionallyOwnedEntity with ShareableEntity with DescribedEntity
{
    type DataSourceType = DataSource

    type AnalysisType <: Analysis

    type VisualizerType <: Visualizer

    /*type DataSourceAnalysisBindingType <: DataSourceAnalysisBinding



    type AnalysisTransformerBindingType <: AnalysisTransformerBinding

    type AnalysisVisualizerBindingType <: AnalysisVisualizerBinding

    type TransformerType <: Transformer

    type TransformerTransformerBindingType <: TransformerTransformerBinding

    type TransformerVisualizerBindingType <: TransformerVisualizerBinding

    */

    protected var _dataSources = mutable.ArrayBuffer[DataSourceType]()

    //protected var _dataSourceAnalysisBindings = mutable.ArrayBuffer[DataSourceAnalysisBindingType]()

    protected var _analysis: AnalysisType

    //protected var _analysisTransformerBinding = mutable.ArrayBuffer[AnalysisTransformerBindingType]()

    //protected var _analysisVisualizerBinding = mutable.ArrayBuffer[AnalysisVisualizerBindingType]()

    //protected var _transformers = mutable.ArrayBuffer[TransformerType]()

    //protected var _transformerTransformerBindings = mutable.ArrayBuffer[TransformerTransformerBindingType]()

    //protected var _transformerVisualizerBinding = mutable.ArrayBuffer[TransformerVisualizerBindingType]()

    //protected var _visualizer = mutable.ArrayBuffer[VisualizerType]()

    def dataSources: immutable.Seq[DataSourceType] = _dataSources.toList

    /**
     * Stores the specified data source to the pipeline.
     * @param ds The data source to store.
     */
    protected def storeDataSource(ds: DataSourceType) {
        _dataSources += ds
    }

    /**
     * Discards the specified data source from the pipeline. Complementary operation to store.
     * @param ds The data source to discard.
     */
    protected def discardDataSource(ds: DataSourceType) {
        _dataSources -= ds
    }

    //def dataSourceAnalysisBindings: immutable.Seq[DataSourceAnalysisBindingType] = _dataSourceAnalysisBindings.toList

    def analysis = _analysis

    //def analysisTransformerBinding: immutable.Seq[AnalysisTransformerBindingType] = _analysisTransformerBinding.toList

    //def analysisVisualizerBinding: immutable.Seq[AnalysisVisualizerBindingType] = _analysisVisualizerBinding.toList

    //def transformers: immutable.Seq[TransformerType] = _transformers.toList

    //def transformerTransformerBindings: immutable.Seq[TransformerTransformerBindingType] = _transformerTransformerBindings.toList

    //def transformerVisualizerBinding: immutable.Seq[TransformerVisualizerBindingType] = _transformerVisualizerBinding.toList

    //def visualizer: immutable.Seq[VisualizerType] = _visualizer.toList
}