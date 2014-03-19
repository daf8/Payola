package cz.payola.common

import cz.payola.common.rdf.Graph
import scala.collection.immutable
import cz.payola.common.entities.plugins.PluginInstance

/**
 * Result of data sources Check with ASK queries.
 */
abstract class CheckState

//case class CheckInProgress(value: Double, evaluatedInstances: immutable.Seq[PluginInstance],
//    runningInstances: Seq[(PluginInstance, Double)],
//    errors: Seq[(PluginInstance, String)]) extends CheckState


case class CheckError(error: String, instanceErrors: Seq[(PluginInstance, String)]) extends CheckState


case class CheckSuccess(result: Boolean = false) extends CheckState


class CheckInProgress extends CheckState

