package ie.propertyhub.repository;

import ie.propertyhub.domain.Listing;
import java.time.Clock;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.stereotype.Repository;

/**
 * Thread-safe in-memory listing store, seeded with demo data on start-up.
 * Kept deliberately simple so the container is stateless; swap for a JPA repository + RDS in production.
 */
@Repository
public class ListingRepository {

    private final Map<Long, Listing> store = new ConcurrentHashMap<>();
    private final AtomicLong sequence = new AtomicLong();

    public ListingRepository(Clock clock) {
        SampleData.listings(LocalDate.now(clock)).forEach(this::save);
    }

    public Listing save(Listing listing) {
        Listing toSave = listing.id() == null ? listing.withId(sequence.incrementAndGet()) : listing;
        store.put(toSave.id(), toSave);
        return toSave;
    }

    public Optional<Listing> findById(long id) {
        return Optional.ofNullable(store.get(id));
    }

    public List<Listing> findAll() {
        return store.values().stream().sorted(Comparator.comparing(Listing::id)).toList();
    }

    public int count() {
        return store.size();
    }
}
