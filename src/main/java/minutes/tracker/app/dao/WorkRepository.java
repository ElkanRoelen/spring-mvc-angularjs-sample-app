package minutes.tracker.app.dao;


import org.apache.log4j.Logger;
import org.springframework.stereotype.Repository;

import minutes.tracker.app.model.Work;
import minutes.tracker.app.model.User;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.*;
import java.sql.Time;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 *
 * Repository class for the Work entity
 *
 */
@Repository
public class WorkRepository {

    private static final Logger LOGGER = Logger.getLogger(WorkRepository.class);

    @PersistenceContext
    EntityManager em;

    /**
     *
     * counts the matching works, given the bellow criteria
     *
     * @param username - the currently logged in username
     * @param fromDate - search from this date, including
     * @param toDate - search until this date, including
     * @param fromTime - search from this time, including
     * @param toTime - search to this time, including
     * @return -  a list of matching works, or an empty collection if no match found
     */
    public Long countWorksByDateTime(String username, Date fromDate, Date toDate, Time fromTime, Time toTime) {

        CriteriaBuilder cb = em.getCriteriaBuilder();

        // query for counting the total results
        CriteriaQuery<Long> cq = cb.createQuery(Long.class);
        Root<Work> countRoot = cq.from(Work.class);
        cq.select((cb.count(countRoot)));
        cq.where(getCommonWhereCondition(cb, username, countRoot, fromDate, toDate, fromTime, toTime));
        Long resultsCount = em.createQuery(cq).getSingleResult();

        LOGGER.info("Found " + resultsCount + " results.");

        return resultsCount;
    }

    /**
     *
     * finds a list of works, given the bellow criteria
     *
     * @param username - the currently logged in username
     * @param fromDate - search from this date, including
     * @param toDate - search until this date, including
     * @param fromTime - search from this time, including
     * @param toTime - search to this time, including
     * @return -  a list of matching works, or an empty collection if no match found
     */
    public List<Work> findWorksByDateTime(String username, Date fromDate, Date toDate,
                                          Time fromTime, Time toTime, int pageNumber) {

        CriteriaBuilder cb = em.getCriteriaBuilder();

        // the actual search query that returns one page of results
        CriteriaQuery<Work> searchQuery = cb.createQuery(Work.class);
        Root<Work> searchRoot = searchQuery.from(Work.class);
        searchQuery.select(searchRoot);
        searchQuery.where(getCommonWhereCondition(cb, username, searchRoot, fromDate, toDate, fromTime, toTime));

        List<Order> orderList = new ArrayList();
        orderList.add(cb.desc(searchRoot.get("date")));
        orderList.add(cb.asc(searchRoot.get("time")));
        searchQuery.orderBy(orderList);

        TypedQuery<Work> filterQuery = em.createQuery(searchQuery)
                .setFirstResult((pageNumber - 1) * 10)
                .setMaxResults(10);

        return filterQuery.getResultList();
    }

    /**
     * Delete a work, given its identifier
     *
     * @param deletedWorkId - the id of the work to be deleted
     */
    public void delete(Long deletedWorkId) {
        Work delete = em.find(Work.class, deletedWorkId);
        em.remove(delete);
    }

    /**
     *
     * finds a work given its id
     *
     */
    public Work findWorkById(Long id) {
        return em.find(Work.class, id);
    }

    /**
     *
     * save changes made to a work, or create the work if its a new work.
     *
     */
    public Work save(Work work) {
        return em.merge(work);
    }


    private Predicate[] getCommonWhereCondition(CriteriaBuilder cb, String username, Root<Work> searchRoot, Date fromDate, Date toDate,
                                                Time fromTime, Time toTime) {

        List<Predicate> predicates = new ArrayList<>();
        Join<Work, User> user = searchRoot.join("user");

        predicates.add(cb.equal(user.<String>get("username"), username));
        predicates.add(cb.greaterThanOrEqualTo(searchRoot.<Date>get("date"), fromDate));

        if (toDate != null) {
            predicates.add(cb.lessThanOrEqualTo(searchRoot.<Date>get("date"), toDate));
        }

        if (fromTime != null) {
            predicates.add(cb.greaterThanOrEqualTo(searchRoot.<Date>get("time"), fromTime));
        }

        if (toTime != null) {
            predicates.add(cb.lessThanOrEqualTo(searchRoot.<Date>get("time"), toTime));
        }

        return predicates.toArray(new Predicate[]{});
    }

}
