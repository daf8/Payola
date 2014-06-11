package cz.payola.data.squeryl.entities

//import cz.payola.data.squeryl.entities.pipelines._
import scala.collection.immutable
import scala.collection.mutable
import cz.payola.data.squeryl._
import cz.payola.common.entities.Pipeline
import cz.payola.data.squeryl.entities.plugins.DataSource

/**
 * This object converts [[cz.payola.common.entities.Pipeline]] to [[cz.payola.data.squeryl.entities.Pipeline]]
 */
object Pipeline extends EntityConverter[Pipeline]
{
    def convert(entity: AnyRef)(implicit context: SquerylDataContextComponent): Option[Pipeline] = {
        entity match {
            case e: Pipeline => Some(e)
            case e: cz.payola.common.entities.Pipeline
            => Some(new Pipeline(e.id, e.name, e.owner.map(User(_)), e.isPublic, e.description, Analysis(e.analysis)))
            case _ => None
        }
    }
}

/**
 * Provides database persistence to [[cz.payola.domain.entities.Pipeline]] entities.
 * @param id ID of the pipeline
 * @param name Name of the pipeline
 * @param o Owner of the pipeline
 * @param _isPub Whether the pipeline is public or not
 * @param _desc Description of the pipeline
 * @param context Implicit context
 * @param a Analysis
 */
class Pipeline(
    override val id: String, name: String, o: Option[User], var _isPub: Boolean, var _desc: String, a: Analysis)
    (implicit val context: SquerylDataContextComponent)
    extends cz.payola.domain.entities.Pipeline(name, o, a)
    with Entity with OptionallyOwnedEntity with ShareableEntity with DescribedEntity
{
    _dataSources = null

    private lazy val _dataSourcesQuery = context.schema.pipelineDataSource.right(this)

    val analysisId: String = Option(a).map(_.id).getOrElse(null)

    def analysis_=(value: AnalysisType) {
        _analysis = value
    }

    /*_dataSourceAnalysisBindings = null

    private lazy val _dataSourceAnalysisBindingsQuery = context.schema.pipelinesDataSourceAnalysisBindings.left(this)

    _analysis = null

    private lazy val _analysisQuery = context.schema.pipelinesAnalysis.left(this)

    _analysisTransformerBinding = null

    private lazy val _analysisTransformerBindingsQuery = context.schema.pipelinesAnalysisTransformerBindings.left(this)

    _analysisVisualizerBinding = null

    private lazy val _analysisVisualizerBindingsQuery = context.schema.pipelinesAnalysisVisualizerBindings.left(this)

    _transformers = null

    private lazy val _transformersQuery = context.schema.pipelinesTransformers.left(this)

    _transformerTransformerBindings = null

    private lazy val _transformerTransformerBindingsQuery = context.schema.pipelinesTransformerTransformerBindings.left(this)

    _transformerVisualizerBinding = null

    private lazy val _transformerVisualizerBindingsQuery = context.schema.pipelinesTransformerVisualizerBindings.left(this)

    _visualizer = null

    private lazy val _visualizerQuery = context.schema.pipelinesVisualizer.left(this)

    override def dataSources: immutable.Seq[DataSourceType] = {
        if (_dataSources == null) {
            context.pipelineRepository.loadDataSources(this)
        }

        _dataSources.toList
    }*/

    def dataSources_=(value: Seq[DataSourceType]) {
        _dataSources = mutable.ArrayBuffer(value: _*)
    }

    override def storeDataSource(ds: DataSourceType) {
        super.storeDataSource(context.schema.associate(DataSource(ds), context.schema.pipelineDataSource.right(this)))
    }

    override protected def discardDataSource(ds: DataSourceType) {
        super.discardDataSource(context.schema.dissociate(DataSource(ds), context.schema.pipelineDataSource.right(this)))
    }
/*
    override def analysis: AnalysisType = {
        if (_analysis == null) {
            context.pipelineRepository.loadAnalysis(this)
        }

        _analysis
    }

    def analysis_=(value: AnalysisType) {
        _analysis = value
    }

    override def transformers: immutable.Seq[TransformerType] = {
        if (_transformers == null) {
            context.pipelineRepository.loadDataSources(this)
        }

        _transformers.toList
    }

    def transformers_=(value: Seq[TransformerType]) {
        _transformers = mutable.ArrayBuffer(value: _*)
    }

    override def visualizer: AnalysisType = {
        if (_visualizer == null) {
            context.pipelineRepository.loadVisualizer(this)
        }

        _visualizer
    }

    def visualizer_=(value: AnalysisType) {
        _visualizer = value
    }

    override def dataSourceAnalysisBindings: immutable.Seq[DataSourceAnalysisBindingType] = {
        if (_dataSourceAnalysisBindings == null) {
            context.pipelineRepository.loadDataSourceAnalysisBindings(this)
        }

        _dataSourceAnalysisBindings.toList
    }

    def dataSourceAnalysisBindings_=(value: Seq[DataSourceAnalysisBindingType]) {
        _dataSourceAnalysisBindings = mutable.ArrayBuffer(value: _*)
    }

    override def transformerTransformerBindings: immutable.Seq[TransformerTransformerBindingType] = {
        if (_transformerTransformerBindings == null) {
            context.pipelineRepository.loadTransformerTransformerBindings(this)
        }

        _transformerTransformerBindings.toList
    }

    def transformerTransformerBindings_=(value: Seq[TransformerTransformerBindingType]) {
        _transformerTransformerBindings = mutable.ArrayBuffer(value: _*)
    }

    override def analysisTransformerBindings: AnalysisTransformerBindingType = {
        if (_analysisTransformerBindings == null) {
            context.pipelineRepository.loadAnalysisTransformerBindings(this)
        }

        _analysisTransformerBindings
    }

    def analysisTransformerBindings_=(value: AnalysisTransformerBindingType) {
        _analysisTransformerBindings = value
    }

    override def analysisVisualizerBindings: AnalysisVisualizerBindingType = {
        if (_analysisVisualizerBindings == null) {
            context.pipelineRepository.loadAnalysisVisualizerBindings(this)
        }

        _analysisVisualizerBindings
    }

    def analysisVisualizerBindings_=(value: AnalysisVisualizerBindingType) {
        _analysisVisualizerBindings = value
    }

    override def transformerVisualizerBindings: TransformerVisualizerBindingType = {
        if (_transformerVisualizerBindings == null) {
            context.pipelineRepository.loadTransformerVisualizerBindings(this)
        }

        _transformerVisualizerBindings
    }

    def transformerVisualizerBindings_=(value: TransformerVisualizerBindingType) {
        _transformerVisualizerBindings = value
    }

    override protected def storeDataSourceAnalysisBinding(binding: Pipeline#DataSourceAnalysisBindingType) {
        super.storeDataSourceAnalysisBinding(associateDataSourceAnalysisBinding(DataSourceAnalysisBinding(binding)))
    }

    override protected def discardDataSourceAnalysisBinding(binding: Pipeline#DataSourceAnalysisBindingType) {
        context.analysisRepository.removeDataSourceAnalysisBindingById(binding.id)

        super.discardDataSourceAnalysisBinding(binding)
    }

    override protected def storeAnalysisTransformerBinding(binding: Pipeline#AnalysisTransformerBindingType) {
        super.storeAnalysisTransformerBinding(associateAnalysisTransformerBinding(AnalysisTransformerBinding(binding)))
    }

    override protected def discardAnalysisTransformerBinding(binding: Pipeline#AnalysisTransformerBindingType) {
        context.analysisRepository.removeAnalysisTransformerBindingById(binding.id)

        super.discardAnalysisTransformerBinding(binding)
    }

    override protected def storeAnalysisVisualizerBinding(binding: Pipeline#AnalysisVisualizerBindingType) {
        super.storeAnalysisVisualizerBinding(associateAnalysisVisualizerBinding(AnalysisVisualizerBinding(binding)))
    }

    override protected def discardAnalysisVisualizerBinding(binding: Pipeline#AnalysisVisualizerBindingType) {
        context.analysisRepository.removeAnalysisVisualizerBindingById(binding.id)

        super.discardAnalysisVisualizerBinding(binding)
    }

    override protected def storeTransformerTransformerBinding(binding: Pipeline#TransformerTransformerBindingType) {
        super.storeTransformerTransformerBinding(associateTransformerTransformerBinding(TransformerTransformerBinding(binding)))
    }

    override protected def discardTransformerTransformerBinding(binding: Pipeline#TransformerTransformerBindingType) {
        context.analysisRepository.removeTransformerTransformerBindingById(binding.id)

        super.discardTransformerTransformerBinding(binding)
    }

    override protected def storeTransformerVisualizerBinding(binding: Pipeline#TransformerVisualizerBindingType) {
        super.storeTransformerVisualizerBinding(associateTransformerVisualizerBinding(TransformerVisualizerBinding(binding)))
    }

    override protected def discardTransformerVisualizerBinding(binding: Pipeline#TransformerVisualizerBindingType) {
        context.analysisRepository.removeTransformerVisualizerBindingById(binding.id)

        super.discardTransformerVisualizerBinding(binding)
    }

    def associateDataSource(dataSource: DataSource): DataSource = {
        context.schema.associate(dataSource, _dataSourcesQuery)
        //context.pipelineRepository.persistDataSource(dataSource)

        dataSource
    }

    def associateAnalysis(analysis: Analysis): Analysis = {
        context.schema.associate(analysis, _analysisQuery)
        context.pipelineRepository.persistAnalysis(analysis)

        analysis
    }

    def associateTransformer(transformer: Transformer): Transformer = {
        context.schema.associate(transformer, _transformersQuery)
        context.pipelineRepository.persistDataSource(transformer)

        transformer
    }

    def associateVisualizer(visualizer: Visualizer): Visualizer = {
        context.schema.associate(visualizer, _visualizerQuery)
        context.pipelineRepository.persistVisualizer(visualizer)

        visualizer
    }

    def associateDataSourceAnalysisBinding(instance: DataSourceAnalysisBinding): DataSourceAnalysisBinding = {
        context.schema.associate(instance, _dataSourceAnalysisBindingsQuery)
    }

    def associateAnalysisTransformerBinding(instance: AnalysisTransformerBinding): AnalysisTransformerBinding = {
        context.schema.associate(instance, _analysisTransformerBindingsQuery)
    }

    def associateAnalysisVisualizerBinding(instance: AnalysisVisualizerBinding): AnalysisVisualizerBinding = {
        context.schema.associate(instance, _analysisVisualizerBindingsQuery)
    }

    def associateTransformerTransformerBinding(instance: TransformerTransformerBinding): TransformerTransformerBinding = {
        context.schema.associate(instance, _transformerTransformerBindingsQuery)
    }

    def associateTransformerVisualizerBinding(instance: TransformerVisualizerBinding): TransformerVisualizerBinding = {
        context.schema.associate(instance, _transformerVisualizerBindingsQuery)
    }*/
}