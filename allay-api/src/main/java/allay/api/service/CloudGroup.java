package allay.api.service;

import allay.api.interfaces.Named;

import java.util.HashMap;
import java.util.List;

public interface CloudGroup extends Named, Reloadable {

    long memory();
    long minInstances();
    long maxInstances();
    boolean staticGroup();

    String version();
    String javaVersion();

    HashMap<String, Long> nodes();
    List<String> templates();

}
