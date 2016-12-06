package minutes.tracker.app.services;


import minutes.tracker.app.dao.WorkRepository;
import minutes.tracker.app.dao.UserRepository;
import minutes.tracker.app.dto.WorkDTO;
import minutes.tracker.app.model.Work;
import minutes.tracker.app.model.SearchResult;
import minutes.tracker.app.model.User;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Time;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import static minutes.tracker.app.services.ValidationUtils.assertNotBlank;
import static org.springframework.util.Assert.notNull;

/**
 *
 * Business service for Work-related operations.
 *
 */
@Service
public class WorkService {

    private static final Logger LOGGER = Logger.getLogger(WorkService.class);

    @Autowired
    WorkRepository workRepository;

    @Autowired
    UserRepository userRepository;

    /**
     *
     * searches works by date/time
     *
     * @param username - the currently logged in user
     * @param fromDate - search from this date, including
     * @param toDate - search until this date, including
     * @param fromTime - search from this time, including
     * @param toTime - search to this time, including
     * @param pageNumber - the page number (each page has 10 entries)
     * @return - the found results
     */
    @Transactional(readOnly = true)
    public SearchResult<Work> findWorks(String username, Date fromDate, Date toDate, Time fromTime, Time toTime, int pageNumber) {

        if (fromDate == null || toDate == null) {
            throw new IllegalArgumentException("Both the from and to date are needed.");
        }

        if (fromDate.after(toDate)) {
            throw new IllegalArgumentException("From date cannot be after to date.");
        }

        if (fromDate.equals(toDate) && fromTime != null && toTime != null && fromTime.after(toTime)) {
            throw new IllegalArgumentException("On searches on the same day, from time cannot be after to time.");
        }

        Long resultsCount = workRepository.countWorksByDateTime(username, fromDate, toDate, fromTime, toTime);

        List<Work> works = workRepository.findWorksByDateTime(username, fromDate, toDate, fromTime, toTime, pageNumber);

        return new SearchResult<>(resultsCount, works);
    }

    /**
     *
     * deletes a list of works, given their Ids
     *
     * @param deletedWorkIds - the list of works to delete
     */
    @Transactional
    public void deleteWorks(List<Long> deletedWorkIds) {
        notNull(deletedWorkIds, "deletedWorksId is mandatory");
        deletedWorkIds.stream().forEach((deletedWorkId) -> workRepository.delete(deletedWorkId));
    }

    /**
     *
     * saves a work (new or not) into the database.
     *
     * @param username - - the currently logged in user
     * @param id - the database ud of the work
     * @param date - the date the work took place
     * @param time - the time the work took place
     * @param description - the description of the work
     * @param minutes - the minutes of the work
     * @return - the new version of the work
     */

    @Transactional
    public Work saveWork(String username, Long id, Date date, Time time, String description, Long minutes) {

        assertNotBlank(username, "username cannot be blank");
        notNull(date, "date is mandatory");
        notNull(time, "time is mandatory");
        notNull(description, "description is mandatory");
        notNull(minutes, "minutes is mandatory");

        Work work = null;

        if (id != null) {
            work = workRepository.findWorkById(id);

            work.setDate(date);
            work.setTime(time);
            work.setDescription(description);
            work.setMinutes(minutes);
        } else {
            User user = userRepository.findUserByUsername(username);

            if (user != null) {
                work = workRepository.save(new Work(user, date, time, description, minutes));
                LOGGER.warn("A work was attempted to be saved for a non-existing user: " + username);
            }
        }

        return work;
    }

    /**
     *
     * saves a list of works (new or not) into the database
     *
     * @param username - the currently logged in user
     * @param works - the list of works to be saved
     * @return - the new versions of the saved works
     */
    @Transactional
    public List<Work> saveWorks(String username, List<WorkDTO> works) {
        return works.stream()
                .map((work) -> saveWork(
                        username,
                        work.getId(),
                        work.getDate(),
                        work.getTime(),
                        work.getDescription(),
                        work.getMinutes()))
                .collect(Collectors.toList());
    }
}
