import io.atomix.catalyst.buffer.BufferInput;
import io.atomix.catalyst.buffer.BufferOutput;
import io.atomix.catalyst.serializer.CatalystSerializable;
import io.atomix.catalyst.serializer.Serializer;

public class Prepare implements CatalystSerializable {
    String transact;

    public Prepare() {}
    public Prepare(String transact) {
        this.transact = transact;
    }

    @Override
    public void writeObject(BufferOutput<?> bufferOutput, Serializer serializer) {
        bufferOutput.writeString(transact);
    }

    @Override
    public void readObject(BufferInput<?> bufferInput, Serializer serializer) {
        transact = bufferInput.readString();
    }
}