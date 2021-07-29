package māia.topology.util

import māia.configure.initialise
import māia.configure.util.readConfiguration
import māia.configure.visitation.ConfigurationVisitable
import māia.topology.Node
import māia.topology.Topology
import māia.topology.visitation.TopologyVisitable
import māia.topology.visitation.TopologyVisitor
import māia.topology.visitation.visit
import kotlin.reflect.KClass

/**
 * Creates a topology from a visitable source.
 *
 * @param source    The topology-visitable source.
 * @return          The topology.
 */
fun readTopology(source : TopologyVisitable) : Topology {
    return TopologyReader().visit(source).result
}

/**
 * Builds a topology by visiting another topology-visitable source.
 */
private class TopologyReader : TopologyVisitor {

    /** The resulting topology. */
    lateinit var result : Topology

    /** The builder used to create the topology. */
    lateinit var builder : Topology.Builder

    /** The nodes read from the visitable. */
    val nodes = ArrayList<Node<*>>()

    override fun begin() {
        // Create the topology builder
        builder = Topology.Builder()
    }

    override fun node(type : KClass<out Node<*>>, configuration : ConfigurationVisitable) {
        // Create an instance of the node and save it for later
        nodes.add(type.initialise(readConfiguration(configuration)))
    }

    override fun subscription(fromNode : Int, fromNodeOutputName : String, toNode : Int, toNodeInputName : String) {
        // Add a subscription between the actual node instances to the builder
        builder.addSubscriptionDirect(
                nodes[toNode].inputs[toNodeInputName]!!,
                nodes[fromNode].outputs[fromNodeOutputName]!!
        )
    }

    override fun end() {
        // Stamp the topology
        result = builder.instantiate()
    }

}
