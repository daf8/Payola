package cz.payola.domain.entities.analyses

import cz.payola.common.entities.analyses
import cz.payola.common.entities.plugins.DataSource
import cz.payola.domain.Entity
import cz.payola.domain.entities.analyses
import cz.payola.domain.entities.plugins._

/**
 * @param _sourcePluginInstance The plugin instance that acts as a source of the binding.
 * @param _compatibleDataSource The plugin instance that acts as a target of the binding.
 */
class CompatibilityCheck(
    protected var _sourcePluginInstance: PluginInstance,
    protected var _compatibleDataSource: DataSource)
    extends Entity
    with cz.payola.common.entities.analyses.CompatibilityCheck
{
    checkConstructorPostConditions()

    type PluginInstanceType = PluginInstance

    type DataSourceType = DataSource

    override def canEqual(other: Any): Boolean = {
        other.isInstanceOf[CompatibilityCheck]
    }

    override protected def checkInvariants() {
        super[Entity].checkInvariants()
        validate(sourcePluginInstance != null, "sourcePluginInstance",
            "The source plugin instance of the binding mustn't be null.")
        validate(compatibleDataSource != null, "compatibleDataSource",
            "The compatible data source of the binding mustn't be null.")
        validate(sourcePluginInstance != compatibleDataSource, "sourcePluginInstance",
            "The source plugin instance of the binding cannot also be the target plugin instance (a cycle formed of " +
                "one plugin instance).")
    }
}
