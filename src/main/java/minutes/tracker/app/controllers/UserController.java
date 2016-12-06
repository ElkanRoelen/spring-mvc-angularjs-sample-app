package minutes.tracker.app.controllers;


import minutes.tracker.app.dto.NewUserDTO;
import minutes.tracker.app.dto.UserInfoDTO;
import minutes.tracker.app.model.User;
import minutes.tracker.app.services.UserService;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

/**
 *
 *  REST service for users.
 *
 */

@Controller
@RequestMapping("/user")
public class UserController {

    private static final Logger LOGGER = Logger.getLogger(UserController.class);

    @Autowired
    UserService userService;

    @ResponseBody
    @ResponseStatus(HttpStatus.OK)
    @RequestMapping(method = RequestMethod.GET)
    public UserInfoDTO getUserInfo(Principal principal) {

        User user = userService.findUserByUsername(principal.getName());
        Long todaysMinutes = userService.findTodaysMinutesForUser(principal.getName());

        return user != null ? new UserInfoDTO(user.getUsername(), user.getMaxMinutesPerDay(), todaysMinutes) : null;
    }

    @ResponseBody
    @ResponseStatus(HttpStatus.OK)
    @RequestMapping(method = RequestMethod.PUT)
    public void updateUserMaxMinutesPerDay(Principal principal, @RequestBody Long newMaxMinutes) {
        userService.updateUserMaxMinutesPerDay(principal.getName(), newMaxMinutes);
    }


    @ResponseStatus(HttpStatus.OK)
    @RequestMapping(method = RequestMethod.POST)
    public void createUser(@RequestBody NewUserDTO user) {
        userService.createUser(user.getUsername(), user.getEmail(), user.getPlainTextPassword());
    }


    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> errorHandler(Exception exc) {
        LOGGER.error(exc.getMessage(), exc);
        return new ResponseEntity<>(exc.getMessage(), HttpStatus.BAD_REQUEST);
    }

}
