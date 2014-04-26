package cz.payola.domain.entities.transformers

import cz.payola.domain.DomainException

class TransformerException(message: String = "", cause: Throwable = null) extends DomainException(message, cause)
