package bank;

import bank.requests.*;
import io.atomix.catalyst.concurrent.SingleThreadContext;
import io.atomix.catalyst.transport.Address;
import io.atomix.catalyst.transport.Connection;
import io.atomix.catalyst.transport.Transport;
import io.atomix.catalyst.transport.netty.NettyTransport;

import java.util.concurrent.ExecutionException;

public class RemoteAccount implements Account {

    private final SingleThreadContext tc;
    private final Address address;
    private final Connection c;
    public int id;

    public RemoteAccount(SingleThreadContext tc, Address address, int id) {
        this.tc = tc;
        this.id = id;
        this.address = address;
        Transport t = new NettyTransport();
        Connection connection = null;
        try {
            connection = tc.execute(() ->
                    t.client().connect(address)
            ).join().get();
        } catch(InterruptedException|ExecutionException e) {
            e.printStackTrace();
        }
        c = connection;
    }

    @Override
    public String getIban() {
        AccountInfoRep rep = null;
        try {
            rep = (AccountInfoRep) tc.execute(() ->
                    c.sendAndReceive(new AccountInfoReq(1, id))
            ).join().get();
        } catch (InterruptedException|ExecutionException e) {
            e.printStackTrace();
        }
        if(rep == null) return null;
        return rep.iban;
    }

    @Override
    public boolean buy(float price) {
        BankTxnRep r = null;
        try {
            r = (BankTxnRep) tc.execute(() ->
                    c.sendAndReceive(new BankTxnReq(id, price))
            ).join().get();
        } catch (InterruptedException|ExecutionException e) {
            e.printStackTrace();
        }
        return r.result;
    }
}