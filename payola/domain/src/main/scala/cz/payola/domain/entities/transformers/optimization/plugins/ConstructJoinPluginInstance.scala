package cz.payola.domain.entities.transformers.optimization.plugins

import cz.payola.domain.entities.plugins._
import cz.payola.domain.entities.plugins.concrete.query.Construct
import cz.payola.domain.entities.plugins.concrete.Join
import cz.payola.domain.entities.transformers.optimization.TransformerPluginWithInstance

/**
  * An instance of the construct join optimization plugin.
  * @param join The join plugin instance with its plugin.
  * @param subjectConstruct The construct plugin instance with its plugin corresponding to the subject in the join.
  * @param objectConstruct The construct plugin instance with its plugin corresponding to the object in the join.
  */
class ConstructJoinPluginInstance(
    val join: TransformerPluginWithInstance[Join],
    val subjectConstruct: TransformerPluginWithInstance[Construct],
    val objectConstruct: TransformerPluginWithInstance[Construct])
    extends TransformerPluginInstance(ConstructJoinPlugin, Nil)
