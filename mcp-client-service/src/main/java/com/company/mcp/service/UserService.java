package com.company.mcp.service;

import org.springframework.stereotype.Service;



import com.company.mcp.entity.Channel;
import com.company.mcp.entity.User;
import com.company.mcp.repository.ChannelRepository;
import com.company.mcp.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.*;
import java.util.function.*;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final ChannelRepository channelRepository;

    public UserService(UserRepository userRepository, ChannelRepository channelRepository) {
        this.userRepository = userRepository;
        this.channelRepository = channelRepository;
    }

    // ─── Simple DTO to hold name + email only ────────────────────────
    public record UserSummary(String name, String email) {}

    public enum AgeGroup {
        YOUNG("18-25"),
        MID("26-35"),
        SENIOR("36+");

        private final String label;
        AgeGroup(String label) { this.label = label; }
        public String getLabel() { return label; }
    }


    public List<Channel> getTop3PopularChannels() {
        return channelRepository.findAll()
                .stream()
                .sorted(Comparator.comparingInt(
                        (Channel c) -> c.getUsers().size()).reversed()
                )
                .limit(3)
                .collect(Collectors.toList());
    }


    public List<User> getUsersFromTop3Channels() {
        return getTop3PopularChannels()
                .stream()
                .flatMap(channel -> channel.getUsers().stream())    // flatten channel → users
                .distinct()                                         // remove duplicate users
                .collect(Collectors.toList());
    }


    public Map<AgeGroup, List<User>> groupUsersByAgeGroup() {
        return getUsersFromTop3Channels()
                .stream()
                .collect(Collectors.groupingBy(user -> {
                    int age = user.getAge();
                    if (age >= 18 && age <= 25) return AgeGroup.YOUNG;
                    else if (age >= 26 && age <= 35) return AgeGroup.MID;
                    else return AgeGroup.SENIOR;
                }));
    }


    public List<UserSummary> mapUsersToSummary() {
        return getUsersFromTop3Channels()
                .stream()
                .map(user -> new UserSummary(user.getName(), user.getEmail())) // map to summary
                .collect(Collectors.toList());
    }

    public List<UserSummary> getYoungUserSummaries() {
        return getUsersFromTop3Channels()
                .stream()
                .filter(user -> user.getAge() >= 18 && user.getAge() <= 25) // filter young
                .map(user -> new UserSummary(user.getName(), user.getEmail())) // map to summary
                .sorted(Comparator.comparing(UserSummary::name))             // sort by name A-Z
                .collect(Collectors.toList());
    }

    // ────────────────────────────────────────────────────────────────
    // 6. COUNT USERS PER AGE GROUP
    // ────────────────────────────────────────────────────────────────
    public Map<AgeGroup, Long> countUsersPerAgeGroup() {
        return getUsersFromTop3Channels()
                .stream()
                .collect(Collectors.groupingBy(
                        user -> {
                            int age = user.getAge();
                            if (age >= 18 && age <= 25) return AgeGroup.YOUNG;
                            else if (age >= 26 && age <= 35) return AgeGroup.MID;
                            else return AgeGroup.SENIOR;
                        },
                        Collectors.counting()                                    // count instead of list
                ));
    }


    public IntSummaryStatistics getAgeStatistics() {
        return getUsersFromTop3Channels()
                .stream()
                .mapToInt(User::getAge)                                      // convert to IntStream
                .summaryStatistics();                                        // min, max, avg, count
    }

    public Optional<User> findUserByEmail(String email) {
        return userRepository.findAll()
                .stream()
                .filter(user -> user.getEmail().equalsIgnoreCase(email))
                .findFirst();                                                // returns Optional
    }


    public UserSummary getUserSummaryByEmailOrDefault(String email) {
        return findUserByEmail(email)
                .map(user -> new UserSummary(user.getName(), user.getEmail())) // if present map
                .orElse(new UserSummary("Unknown", "unknown@email.com"));      // if empty default
    }


    public UserSummary getUserSummaryOrThrow(String email) {
        return findUserByEmail(email)
                .map(user -> new UserSummary(user.getName(), user.getEmail()))
                .orElseThrow(() -> new RuntimeException("User not found: " + email));
    }


    public Map<String, String> getEmailToNameMap() {
        return getUsersFromTop3Channels()
                .stream()
                .collect(Collectors.toMap(
                        User::getEmail,                                          // key = email
                        User::getName,                                           // value = name
                        (existing, duplicate) -> existing                        // handle duplicates
                ));
    }


    public Map<Boolean, List<User>> partitionUsersByAge() {
        return getUsersFromTop3Channels()
                .stream()
                .collect(Collectors.partitioningBy(
                        user -> user.getAge() <= 25                              // true = young, false = older
                ));
    }


    public boolean hasAnyUnderageUser() {
        return getUsersFromTop3Channels()
                .stream()
                .anyMatch(user -> user.getAge() < 18);                       // true if any under 18
    }

    public boolean areAllUsersAdults() {
        return getUsersFromTop3Channels()
                .stream()
                .allMatch(user -> user.getAge() >= 18);                      // true if all 18+
    }

    public boolean hasNoSeniorUsers() {
        return getUsersFromTop3Channels()
                .stream()
                .noneMatch(user -> user.getAge() > 60);                      // true if none over 60
    }


    public String getAllUserNamesAsString() {
        return getUsersFromTop3Channels()
                .stream()
                .map(User::getName)
                .reduce("", (a, b) -> a.isEmpty() ? b : a + ", " + b);      // join with comma
    }


    public String getAllUserNamesJoined() {
        return getUsersFromTop3Channels()
                .stream()
                .map(User::getName)
                .collect(Collectors.joining(", "));                          // cleaner join
    }


    public Optional<User> findOldestUser() {
        return getUsersFromTop3Channels()
                .stream()
                .max(Comparator.comparingInt(User::getAge));                 // returns Optional<User>
    }


    public Optional<User> findYoungestUser() {
        return getUsersFromTop3Channels()
                .stream()
                .min(Comparator.comparingInt(User::getAge));                 // returns Optional<User>
    }


    public Map<AgeGroup, List<UserSummary>> getGroupedUserSummaries() {
        return getUsersFromTop3Channels()
                .stream()
                .collect(Collectors.groupingBy(
                        user -> {
                            int age = user.getAge();
                            if (age >= 18 && age <= 25) return AgeGroup.YOUNG;
                            else if (age >= 26 && age <= 35) return AgeGroup.MID;
                            else return AgeGroup.SENIOR;
                        },
                        Collectors.mapping(                                      // downstream collector
                                user -> new UserSummary(user.getName(), user.getEmail()),
                                Collectors.toList()
                        )
                ));
    }
}