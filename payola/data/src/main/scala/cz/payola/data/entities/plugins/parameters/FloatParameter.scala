package cz.payola.data.entities.plugins.parameters

import cz.payola.data.entities.plugins.Parameter
import cz.payola.data.PayolaDB

/**
  * This objects converts [[cz.payola.common.entities.plugins.parameters.FloatParameter]]
  * to [[cz.payola.data.entities.plugins.parameters.FloatParameter]]
  */
object FloatParameter
{
    def apply(p: cz.payola.common.entities.plugins.parameters.FloatParameter): FloatParameter = {
        p match {
            case p: FloatParameter => p
            case _ => new FloatParameter(p.id, p.name, p.defaultValue)
        }
    }
}

class FloatParameter(
    override val id: String,
    name: String,
    defaultVal: Float)
    extends cz.payola.domain.entities.plugins.parameters.FloatParameter(name, defaultVal)
    with Parameter[Float]
{
    private lazy val _valuesQuery = PayolaDB.valuesOfFloatParameters.left(this)

    // Get, store and set default value of parameter to Database
    val _defaultValueDb = defaultVal

    override def defaultValue = _defaultValueDb

    def parameterValues: Seq[FloatParameterValue] = evaluateCollection(_valuesQuery)

    /**
      * Associates specified [[cz.payola.data.entities.plugins.parameters.FloatParameter]].
      *
      * @param p - [[cz.payola.data.entities.plugins.parameters.FloatParameter]] to associate
      */
    def associateParameterValue(p: FloatParameterValue) {
        associate(p, _valuesQuery)
    }
}


