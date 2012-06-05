package cz.payola.data.entities

import cz.payola.data.dao._
import cz.payola.data.entities.analyses.parameters._
import cz.payola.data.entities.analyses._
import cz.payola.data.PayolaDB
import cz.payola.domain.entities.analyses.plugins.data.SparqlEndpoint
import cz.payola.domain.entities.analyses.plugins.query._
import cz.payola.domain.entities.analyses.plugins._
import cz.payola.domain.entities.analyses.evaluation.Success

object TestObject
{
    println("Connecting ...")
    assert (PayolaDB.connect())

    println("Creating schema ...")
    PayolaDB.createSchema();

    def main(args: Array[String]) {   
    
        val userDao = new UserDAO()
    
        val groupDao = new GroupDAO()  

        val u1 = new cz.payola.domain.entities.User("name")
        
        println("Persisting user ...")
        val user = userDao.persist(u1).get

        // Update test
        user.email = "email"
        user.password = "password"
        val x = userDao.persist(user).get
        assert(x.email == "email")
        assert(x.password == "password")

        val u = userDao.getById(user.id)
        assert(u != None)
        assert(u.get.id == user.id)
        assert(u.get.name == user.name)
        assert(u.get.password == user.password)
        assert(u.get.email == user.email)

        // Test userDao
        assert(userDao.findByUsername("n", 0, 1)(0).id == user.id)
        assert(userDao.findByUsername("a", 0, 1)(0).id == user.id)
        assert(userDao.findByUsername(user.name, 0, 1)(0).id == user.id)
        assert(userDao.findByUsername("invalid name").size == 0)
        assert(userDao.getUserByCredentials(user.name, user.password).get.id == user.id)
        assert(userDao.getUserByCredentials("invalid", "credientals") == None)

        println("Persisting groups ...")

        val g1 = new cz.payola.domain.entities.Group("group1", u1)
        val g2 = new cz.payola.domain.entities.Group("group2", user)
        val group1 = groupDao.persist(g1).get
        val group2 = groupDao.persist(g2).get

        val g = groupDao.getById(group1.id)
        assert(g != None)
        assert(g.get.id == group1.id)
        assert(g.get.name == group1.name)
        assert(g.get.ownerId.get == user.id)

        assert(user.ownedGroups.size == 2)

        println("   Analysis test")
        testAnalysis()

        println("Pagination ...")
        assert(userDao.getAll().size == 1)
        assert(groupDao.getAll().size == 2)
        assert(groupDao.getAll(1, 2).size == 1)
        assert(groupDao.getAll(2, 5).size == 0)                                 
        assert(groupDao.getAll(1, 0).size == 0)
    }
    
    def testAnalysis() {
        val sparqlEndpointPlugin = new SparqlEndpoint
        val concreteSparqlQueryPlugin = new ConcreteSparqlQuery
        val projectionPlugin = new Projection
        val selectionPlugin = new Selection
        val typedPlugin = new Typed
        val join = new Join
        val unionPlugin = new Union

        val analysisDao = new AnalysisDAO
        val plugDao = new PluginDAO
        val plugInstDao = new PluginInstanceDAO
        val userDao = new UserDAO()

        val plugins = List(
            sparqlEndpointPlugin,
            concreteSparqlQueryPlugin,
            projectionPlugin,
            selectionPlugin,
            typedPlugin,
            join,
            unionPlugin
        )

        println("       persisting analysis")
        // persist analysis
        val user = userDao.findByUsername("name", 0, 1)(0)
        val a = new cz.payola.domain.entities.Analysis("Cities with more than 2 million habitants with countries", Some(user))
        val analysis = analysisDao.persist(a).get

        assert(analysisDao.getById(analysis.id).isDefined)
        assert(user.ownedAnalyses.size == 1)

        println("       persisting plugins")
        // Persist  plugins
        for (p <- plugins) {
            val p1 = plugDao.persist(p).get
            assert(p1.id == p.id)

            val p2 = plugDao.getByName(p.name).get
            assert(p1.id == p2.id)
            assert(p2.parameters.size == p.parameters.size)
            assert(p1.parameters.size == p.parameters.size)

            // assert all parameters have proper IDs
            for(param <- p2.parameters){
                assert(p.parameters.find(_.id == param.id).get.name == param.name)
                assert(p.parameters.find(_.id == param.id).get.defaultValue == param.defaultValue)
            }
            
            // assert all parameters have proper IDs
            for(param <- p1.parameters){
                assert(p.parameters.find(_.id == param.id).get.name == param.name)
                assert(p.parameters.find(_.id == param.id).get.defaultValue == param.defaultValue)
            }
        }

        println("       declaring analysis")
        val citiesFetcher = sparqlEndpointPlugin.createInstance().setParameter("EndpointURL", "http://dbpedia.org/sparql")
        val citiesTyped = typedPlugin.createInstance().setParameter("TypeURI", "http://dbpedia.org/ontology/City")
        val citiesProjection = projectionPlugin.createInstance().setParameter("PropertyURIs", List(
            "http://dbpedia.org/ontology/populationDensity", "http://dbpedia.org/ontology/populationTotal"
        ).mkString("\n"))
        val citiesSelection = selectionPlugin.createInstance().setParameter(
            "PropertyURI", "http://dbpedia.org/ontology/populationTotal"
        ).setParameter(
            "Operator", ">"
        ).setParameter(
            "Value", "2000000"
        )
        analysis.addPluginInstances(citiesFetcher, citiesTyped, citiesProjection, citiesSelection)
        analysis.addBinding(citiesFetcher, citiesTyped)
        analysis.addBinding(citiesTyped, citiesProjection)
        analysis.addBinding(citiesProjection, citiesSelection)

        val countriesFetcher = sparqlEndpointPlugin.createInstance().setParameter("EndpointURL", "http://dbpedia.org/sparql")
        val countriesTyped = typedPlugin.createInstance().setParameter("TypeURI", "http://dbpedia.org/ontology/Country")
        val countriesProjection = projectionPlugin.createInstance().setParameter("PropertyURIs", List(
            "http://dbpedia.org/ontology/areaTotal"
        ).mkString("\n"))
        analysis.addPluginInstances(countriesFetcher, countriesTyped, countriesProjection)
        analysis.addBinding(countriesFetcher, countriesTyped)
        analysis.addBinding(countriesTyped, countriesProjection)

        val citiesCountriesJoin = join.createInstance().setParameter(
            "JoinPropertyURI", "http://dbpedia.org/ontology/country"
        ).setParameter(
            "IsInner", false
        )
        analysis.addPluginInstances(citiesCountriesJoin)
        analysis.addBinding(citiesSelection, citiesCountriesJoin, 0)
        analysis.addBinding(countriesProjection, citiesCountriesJoin, 1)

        println("       asserting persisted analysis")
        // Get analysis from DB
        val persistedAnalysis = analysisDao.getById(analysis.id).get
        println("bindings: DB (" + persistedAnalysis.pluginInstanceInputBindings.size + ") vs A (" + analysis.pluginInstanceInputBindings.size + ")")
        assert(persistedAnalysis.pluginInstances.size == analysis.pluginInstances.size)
        assert(persistedAnalysis.pluginInstanceBindings.size == analysis.pluginInstanceBindings.size)
        
        val pluginInstances = List(
            citiesFetcher,
            citiesTyped,
            citiesProjection,
            citiesSelection,
            countriesFetcher,
            countriesTyped,
            countriesProjection,
            citiesCountriesJoin
        )
        
        for(pi <- pluginInstances) {
            val pi2 = plugInstDao.getById(pi.id)
            assert(pi2.isDefined)
            assert(pi2.get.id == pi.id)
            assert(pi2.get.plugin.id == pi.plugin.id)
            assert(pi2.get.parameterValues.size == pi.parameterValues.size)

            // assert all parameters have proper IDs
            for(paramValue <- pi2.get.parameterValues){
                assert(pi.parameterValues.find(_.id == paramValue.id).get.parameter.id == paramValue.parameter.id)
                assert(pi.parameterValues.find(_.id == paramValue.id).get.value == paramValue.value)

                println("ID: " + pi.parameterValues.find(_.id == paramValue.id).get.parameter.id + " vs " + paramValue.parameter.id)
                println("Value: " + pi.parameterValues.find(_.id == paramValue.id).get.value + " vs " + paramValue.value)
            }
        }
        /*
        // .. and test it
        println("       evaluating persisted analysis")
        val eval = persistedAnalysis.get.evaluate()
        while (!eval.isFinished) {
            println("Not finished, current progress: " + eval.progress.value)
            Thread.sleep(1000)
        }
        val res = eval.result

        println("Done with result: " + res.toString)
        assert(res.map(_.isInstanceOf[Success]).getOrElse(false))
        */
    }
}