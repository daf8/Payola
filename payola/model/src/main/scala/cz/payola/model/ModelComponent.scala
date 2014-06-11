package cz.payola.model

import cz.payola.common.Entity
import cz.payola.domain._
import cz.payola.data.DataContextComponent
import cz.payola.model.components._

trait ModelComponent
    extends UserModelComponent
    with GroupModelComponent
    with AnalysisModelComponent
    with TransformerModelComponent
    with AnalysisResultStorageModelComponent
    with TransformerResultStorageModelComponent
    with PluginModelComponent
    with DataSourceModelComponent
    with OntologyCustomizationModelComponent
    with UserCustomizationModelComponent
    with PayolaStorageModelComponent
    with PrivilegeModelComponent
    with DataCubeModelComponent
    with GeocodeModelComponent
    with PrefixModelComponent
    with VisualizerModelComponent
    with PipelineModelComponent
{
    self: DataContextComponent with RdfStorageComponent with PluginCompilerComponent =>

    def persistEntity(e: Entity) {
        repositoryRegistry(e).persist(e)
    }
}
