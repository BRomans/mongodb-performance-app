package it.tai.services;

import it.tai.domain.Elaboration;

import java.util.Optional;

/**
 * Created by miche on 27/10/2016.
 */
public interface LoadService {

    public final static int ELABORATION_TYPE_PUT = 1;
    public final static int ELABORATION_TYPE_GET = 2;

    Optional<Elaboration> getCurrentElaboration();

    Optional<Elaboration> startElaboration(long numOfEntries, int parallelism, int elaborationTypes);

    Optional<Elaboration> stopElaboration();
}