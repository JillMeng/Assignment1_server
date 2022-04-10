import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import org.apache.commons.pool2.BasePooledObjectFactory;
import org.apache.commons.pool2.DestroyMode;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;

import java.io.IOException;

public class ChannelFactory
        extends BasePooledObjectFactory<Channel> {

    private Connection connection;

    public ChannelFactory(Connection connection) {
        this.connection = connection;
    }

    @Override
    public Channel create() throws IOException {
        return connection.createChannel();
    }

    /**
     * Use the default PooledObject implementation.
     */
    @Override
    public PooledObject<Channel> wrap(Channel channel) {
        return new DefaultPooledObject<Channel>(channel);
    }

    @Override
    public void destroyObject(PooledObject<Channel> p, DestroyMode destroyMode) throws Exception {
        super.destroyObject(p, destroyMode);
    }

    // for all other methods, the no-op implementation
    // in BasePooledObjectFactory will suffice
}