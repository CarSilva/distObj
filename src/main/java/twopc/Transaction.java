package twopc;

import pt.haslab.ekit.Clique;
import twopc.requests.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class Transaction {

    private Clique clique;
    private Integer votedCommit;
    private List<Integer> participants;
    private int phase;
    private CompletableFuture<Object> completedCommit;

    public Transaction(Clique clique) {
        votedCommit = 0;
        this.clique = clique;
        this.participants = new ArrayList<>();
        phase = 0;
    }

    public void addParticipant(int participant) {
        boolean exists = false;
        for(Integer i : participants)
            if(i == participant) exists = true;
        if(!exists)
            participants.add(participant);
    }

    public void firstPhase(CompletableFuture<Object> completedCommit, int txid) {
        this.completedCommit = completedCommit;
        send(new Prepare(new TransactInfo(txid, participants)));
        phase = 1;
    }

    public CompletableFuture<Object> getCompletedCommit() {
        return completedCommit;
    }

    public void setCompletedCommit(CompletableFuture<Object> completedCommit) {
        this.completedCommit = completedCommit;
    }

    public void setParticipants(List<Integer> participants) {
        this.participants = participants;
    }

    public void abort(int mytxid) {
        Rollback rb = new Rollback(mytxid);
        send(rb);
        completedCommit.complete(rb);
    }

    public Object voted(Vote vote, int voter) {
        Object res = null;
        if (vote.getVote().equals("ABORT")) {
            res = new Rollback(vote.getTxid());
            send(res, voter);
            completedCommit.complete(res);
        } else {
            synchronized (votedCommit) {
                votedCommit++;
                if (votedCommit == participants.size()) {
                    res = new Commit(vote.getTxid());
                    send(res);
                    completedCommit.complete(res);
                    phase = 2;
                }
            }
        }
        return res;
    }

    public List<Integer> getParticipants() {
        return participants;
    }

    public int getPhase() {
        return phase;

    }

    private void send(Object message) {
        for(Integer i : participants) {
            clique.send(i, message);
        }
        System.out.println("sent");
    }
    private void send(Object message, int except) {
        for(Integer i : participants) {
            if(i != except)
                clique.send(i, message);
        }
    }

}
