package cz.payola.web.client.presenters.components

import cz.payola.web.client.events.EventArgs
import cz.payola.common.rdf.Graph
import cz.payola.common.entities.Transformer

class TransformerEvaluationSuccessEventArgs(target: Transformer, val availableTransformators: List[String])
    extends EventArgs[Transformer](target)
