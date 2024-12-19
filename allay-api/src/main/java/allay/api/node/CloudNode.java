package allay.api.node;

import allay.api.interfaces.ChannelAppender;
import allay.api.interfaces.Named;
import allay.api.interfaces.Reloadable;

public interface CloudNode extends Named, ChannelAppender, Reloadable {

    String hostname();
    String webServer();

}
