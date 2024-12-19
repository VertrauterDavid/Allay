package allay.api.service;

import allay.api.interfaces.Named;
import allay.api.interfaces.Reloadable;
import allay.api.node.CloudNode;

import java.util.List;

public interface CloudGroup extends Named, Reloadable {

    long memory();
    long minInstances();
    long maxInstances();
    boolean staticGroup();

    String version();
    String javaVersion();

    record Node(CloudNode node, long instances) {}
    List<Node> nodes();

    record Template(String name, boolean copyToStatic, int order) {}
    List<Template> templates();

}
