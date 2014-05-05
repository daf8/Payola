package cz.payola.data.squeryl.repositories

import cz.payola.data.squeryl._
import cz.payola.data.squeryl.entities.transformers._
import org.squeryl.PrimitiveTypeMode._
import cz.payola.data.squeryl.entities._
import cz.payola.data.squeryl.entities.plugins._
import cz.payola.domain.entities.settings._
import scala.Some

/**
 * Provides repository to access persisted transformers
 */
trait TransformerRepositoryComponent extends TableRepositoryComponent
{
    self: SquerylDataContextComponent =>
    private lazy val pluginInstanceBindingRepository = new LazyTableRepository[TransformerPluginInstanceBinding](
        schema.transformerPluginInstanceBindings, TransformerPluginInstanceBinding)

    private lazy val compatibilityCheckRepository = new LazyTableRepository[TransformerCompatibilityCheck](
        schema.transformerCompatibilityChecks, TransformerCompatibilityCheck)

    private lazy val compatibilityTransformerCheckRepository = new LazyTableRepository[TransformerToTransformerCompatibilityCheck](
        schema.transformerToTransformerCompatibilityChecks, TransformerToTransformerCompatibilityCheck)

    /**
     * A repository to access persisted transformers
     */
    lazy val transformerRepository = new TransformerDefaultTableRepository

    class TransformerDefaultTableRepository
        extends OptionallyOwnedEntityDefaultTableRepository[Transformer](schema.transformers, Transformer)
        with TransformerRepository
        with NamedEntityTableRepository[Transformer]
        with ShareableEntityTableRepository[Transformer, (Transformer, Option[User])]
        with TransformerPluginInstanceTableRepository[TransformerPluginInstance]
    {
        protected val pluginInstanceLikeTable = schema.transformerPluginInstances

        protected val pluginInstanceLikeEntityConverter = TransformerPluginInstance

        val booleanParameterValuesRelation = schema.booleanParameterValuesOfTransformerPluginInstances

        val floatParameterValuesRelation = schema.floatParameterValuesOfTransformerPluginInstances

        val intParameterValuesRelation = schema.intParameterValuesOfTransformerPluginInstances

        val stringParameterValuesRelation = schema.stringParameterValuesOfTransformerPluginInstances

        protected def getPluginInstanceLikeId(parameterValue: Option[ParameterValue[_]]) = {
            parameterValue.flatMap(_.transformerPluginInstanceId)
        }

        /**
         * When an OntologyCustomization is removed, it should be removed as Default customization from transformers
         * @param customizationId ID of removed OntologyCustomizations
         */
        def ontologyCustomizationIsRemoved(customizationId: String) {
            selectWhere(_.defaultCustomizationId === Some(customizationId)).foreach {a =>
                a.defaultOntologyCustomization = None
                persist(a)
            }
        }

        override def persist(entity: AnyRef): Transformer = wrapInTransaction {
            val transformer = super.persist(entity)

            // Associate plugin instances with their bindings and default customization
            entity match {
                case a: Transformer => // Everything already persisted
                case a: cz.payola.domain.entities.Transformer => {
                    a.pluginInstances.map(pi => transformer.associatePluginInstance(TransformerPluginInstance(pi)))
                    a.pluginInstanceBindings.map(b => transformer.associatePluginInstanceBinding(TransformerPluginInstanceBinding(b)))
                    a.compatibilityChecks.map(c => transformer.associateCompatibilityCheck(TransformerCompatibilityCheck(c)))
                    a.compatibilityTransformerChecks.map(d => transformer.associateTransformerCompatibilityCheck(TransformerToTransformerCompatibilityCheck(d)))
                    transformer.defaultOntologyCustomization = a.defaultOntologyCustomization
                }
            }

            // Return persisted transformer
            transformer
        }

        def removePluginInstanceById(pluginInstanceId: String): Boolean = wrapInTransaction {
            schema.transformerPluginInstances.deleteWhere(e => pluginInstanceId === e.id) == 1
        }

        def removePluginInstanceBindingById(pluginInstanceBindingId: String): Boolean = wrapInTransaction {
            pluginInstanceBindingRepository.removeById(pluginInstanceBindingId)
        }

        def removeCompatibilityCheckById(compatibilityCheckId: String): Boolean = wrapInTransaction {
            compatibilityCheckRepository.removeById(compatibilityCheckId)
        }

        def removeCompatibilityTransformerCheckById(compatibilityCheckId: String): Boolean = wrapInTransaction {
            compatibilityTransformerCheckRepository.removeById(compatibilityCheckId)
        }

        def loadPluginInstances(transformer: Transformer) {
            _loadTransformer(transformer)
        }

        def loadPluginInstanceBindings(transformer: Transformer) {
            _loadTransformer(transformer)
        }

        def loadCompatibilityChecks(transformer: Transformer) {
            _loadTransformer(transformer)
        }

        def loadCompatibilityTransformerChecks(transformer: Transformer) {
            _loadTransformer(transformer)
        }

        def loadDefaultOntology(transformer: Transformer) {
            _loadTransformer(transformer)
        }

        private def _loadTransformer(transformer: Transformer) {
            wrapInTransaction {
                val pluginInstancesByIds =
                    loadPluginInstancesByFilter(pi => pi.asInstanceOf[TransformerPluginInstance].transformerId === transformer.id)
                        .map(p => (p.id, p.asInstanceOf[TransformerPluginInstance])).toMap
                val instanceBindings = pluginInstanceBindingRepository.selectWhere(b => b.transformerId === transformer.id)
                val compatibilityChecks = compatibilityCheckRepository.selectWhere(c => c.transformerId === transformer.id)
                val compatibilityTransformerChecks = compatibilityTransformerCheckRepository.selectWhere(d => d.transformerId === transformer.id)

                // Set plugin instances to bindings
                instanceBindings.foreach {b =>
                    b.sourcePluginInstance = pluginInstancesByIds(b.sourcePluginInstanceId)
                    b.targetPluginInstance = pluginInstancesByIds(b.targetPluginInstanceId)
                }

                compatibilityChecks.foreach {c =>
                    c.sourcePluginInstance = pluginInstancesByIds(c.sourcePluginInstanceId)
                }

                compatibilityTransformerChecks.foreach {d =>
                    d.sourcePluginInstance = pluginInstancesByIds(d.sourcePluginInstanceId)
                }

                // Set loaded plugins, plugin instances and its bindings to transformer, load default customization
                transformer.pluginInstances = pluginInstancesByIds.values.toSeq
                transformer.pluginInstanceBindings = instanceBindings
                transformer.compatibilityChecks = compatibilityChecks
                transformer.compatibilityTransformerChecks = compatibilityTransformerChecks
                transformer.defaultOntologyCustomization = _getDefaultOntologyCustomization(transformer.defaultCustomizationId)
            }
        }

        private def _getDefaultOntologyCustomization(id: Option[String]): Option[OntologyCustomization] = {
            id.flatMap(customizationRepository.getById(_)).flatMap(_.toOntologyCustomization())
        }
    }

}