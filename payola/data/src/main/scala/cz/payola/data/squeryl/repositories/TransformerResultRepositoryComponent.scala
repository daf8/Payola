package cz.payola.data.squeryl.repositories

import cz.payola.data.squeryl._
import cz.payola.data.squeryl.entities.TransformerResult

import org.squeryl.PrimitiveTypeMode._
import s2js.runtime.shared.rpc.RpcException

trait TransformerResultRepositoryComponent extends TableRepositoryComponent {
    self: SquerylDataContextComponent =>

    /**
     * A repository to access persisted results of transformers
     */
    lazy val transformerResultRepository = new TransformerResultDefaultTableRepository

    class TransformerResultDefaultTableRepository
        extends OptionallyOwnedEntityDefaultTableRepository[TransformerResult](schema.transformersResults, TransformerResult)
        with TransformerResultRepository
    {
        def storeResult(transformerDescription: cz.payola.domain.entities.TransformerResult) {
            val converted = entityConverter(transformerDescription)
            wrapInTransaction{
                persist(converted)
            }
        }

        def getResult(evaluationId: String, transformerId: String): Option[TransformerResult] = {
            selectOneWhere(anRes => anRes.transformerId === transformerId and anRes.evaluationId === evaluationId)
        }

        def deleteResult(evaluationId: String, transformerId: String) {
            wrapInTransaction{
                table.deleteWhere(anRes => anRes.transformerId === transformerId and anRes.evaluationId === evaluationId)
            }
        }

        def updateTimestamp(evaluationId: String) {
            wrapInTransaction{
                table.update(anRes =>
                    where(anRes.evaluationId === evaluationId)
                        set(anRes.touched := new java.sql.Timestamp(System.currentTimeMillis))
                )
            }
        }

        def exists(evaluationId: String): Boolean = {
            selectOneWhere(anRes => anRes.evaluationId === evaluationId).isDefined
        }

        def byEvaluationId(evaluationId: String): Option[TransformerResult] = {
            selectOneWhere(anRes => anRes.evaluationId === evaluationId)
        }
    }
}