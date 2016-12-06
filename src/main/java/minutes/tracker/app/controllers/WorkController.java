package minutes.tracker.app.controllers;


import minutes.tracker.app.dto.WorkDTO;
import minutes.tracker.app.dto.WorksDTO;
import minutes.tracker.app.model.Work;
import minutes.tracker.app.model.SearchResult;
import minutes.tracker.app.services.WorkService;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.sql.Time;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 *
 *  REST service for works - allows to update, create and search for works for the currently logged in user.
 *
 */
@Controller
@RequestMapping("work")
public class WorkController {

    Logger LOGGER = Logger.getLogger(WorkController.class);

    private static final long DAY_IN_MS = 1000 * 60 * 60 * 24;


    @Autowired
    private WorkService workService;

    /**
     * search Works for the current user by date and time ranges.
     *
     *
     * @param principal  - the current logged in user
     * @param fromDate - search from this date, including
     * @param toDate - search until this date, including
     * @param fromTime - search from this time, including
     * @param toTime - search to this time, including
     * @param pageNumber - the page number (each page has 10 entries)
     * @return - @see WorksDTO with the current page, total pages and the list of works
     */
    @ResponseBody
    @ResponseStatus(HttpStatus.OK)
    @RequestMapping(method = RequestMethod.GET)
    public WorksDTO searchWorksByDate(
            Principal principal,
            @RequestParam(value = "fromDate", required = false) @DateTimeFormat(pattern = "yyyy/MM/dd") Date fromDate,
            @RequestParam(value = "toDate", required = false) @DateTimeFormat(pattern = "yyyy/MM/dd") Date toDate,
            @RequestParam(value = "fromTime", required = false) @DateTimeFormat(pattern = "yyyy/MM/dd HH:mm") Date fromTime,
            @RequestParam(value = "toTime", required = false) @DateTimeFormat(pattern = "yyyy/MM/dd HH:mm") Date toTime,
            @RequestParam(value = "pageNumber") Integer pageNumber) {

        if (fromDate == null && toDate == null) {
            fromDate = new Date(System.currentTimeMillis() - (3 * DAY_IN_MS));
            toDate = new Date();
        }

        SearchResult<Work> result = workService.findWorks(
                principal.getName(),
                fromDate,
                toDate,
                fromTime != null ? new Time(fromTime.getTime()) : null,
                toTime != null ? new Time(toTime.getTime()) : null,
                pageNumber);

        Long resultsCount = result.getResultsCount();
        Long totalPages = resultsCount / 10;

        if (resultsCount % 10 > 0) {
            totalPages++;
        }

        return new WorksDTO(pageNumber, totalPages, WorkDTO.mapFromWorksEntities(result.getResult()));
    }

    /**
     *
     * saves a list of works - they be either new or existing
     *
     * @param principal - the current logged in user
     * @param works - the list of works to save
     * @return - an updated version of the saved works
     */
    @ResponseBody
    @ResponseStatus(HttpStatus.OK)
    @RequestMapping(method = RequestMethod.POST)
    public List<WorkDTO> saveWorks(Principal principal, @RequestBody List<WorkDTO> works) {

        List<Work> savedWorks = workService.saveWorks(principal.getName(), works);

        return savedWorks.stream()
                .map(WorkDTO::mapFromWorkEntity)
                .collect(Collectors.toList());
    }

    /**
     *
     * deletes a list of works
     *
     * @param deletedWorkIds - the ids of the works to be deleted
     */
    @ResponseBody
    @ResponseStatus(HttpStatus.OK)
    @RequestMapping(method = RequestMethod.DELETE)
    public void deleteWorks(@RequestBody List<Long> deletedWorkIds) {
        workService.deleteWorks(deletedWorkIds);
    }

    /**
     *
     * error handler for backend errors - a 400 status code will be sent back, and the body
     * of the message contains the exception text.
     *
     * @param exc - the exception caught
     */

    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> errorHandler(Exception exc) {
        LOGGER.error(exc.getMessage(), exc);
        return new ResponseEntity<>(exc.getMessage(), HttpStatus.BAD_REQUEST);
    }


}
