package cz.payola.common

import cz.payola.common.rdf.Graph
import scala.collection.immutable
import cz.payola.common.entities.plugins.PluginInstance

/**
 * Result of data sources Check with ASK queries.
 */
abstract class CheckState


case class CheckError(error: String) extends CheckState


case class CheckSuccess(result: Boolean = false) extends CheckState


class CheckInProgress extends CheckState

