package com.sun.xml.ws.util.pipe;

import com.sun.xml.ws.api.message.Packet;
import com.sun.xml.ws.api.pipe.Pipe;
import com.sun.xml.ws.api.pipe.PipeCloner;
import com.sun.xml.ws.api.pipe.helper.AbstractFilterPipeImpl;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.io.PrintStream;

/**
 * {@link Pipe} that dumps messages that pass through.
 *
 * @author Kohsuke Kawaguchi
 */
public class DumpPipe extends AbstractFilterPipeImpl {
    private final PrintStream out;

    private final XMLOutputFactory staxOut;

    /**
     * @param out
     *      The output to send dumps to.
     * @param next
     *      The next {@link Pipe} in the pipeline.
     */
    public DumpPipe(PrintStream out, Pipe next) {
        super(next);
        this.out = out;
        this.staxOut = XMLOutputFactory.newInstance();
    }

    /**
     * Copy constructor.
     */
    private DumpPipe(DumpPipe that, PipeCloner cloner) {
        super(that,cloner);
        this.out = that.out;
        this.staxOut = XMLOutputFactory.newInstance();
    }

    public Packet process(Packet packet) {
        dump("request",packet);
        Packet reply = next.process(packet);
        dump("response",reply);
        return reply;
    }

    private void dump(String header, Packet packet) {
        out.println("====["+header+"]====");
        if(packet.getMessage()==null)
            out.println("(none)");
        else
            try {
                XMLStreamWriter writer = staxOut.createXMLStreamWriter(new PrintStream(out) {
                    public void close() {
                        // noop
                    }
                });
                packet.getMessage().copy().writeTo(writer);
                writer.close();
            } catch (XMLStreamException e) {
                e.printStackTrace(out);
            }
        out.println("============");
    }


    public Pipe copy(PipeCloner cloner) {
        return new DumpPipe(this,cloner);
    }

    public void preDestroy() {
        // noop
    }
}
