package com.example.chat.auth.server.rest.controller;
import com.example.chat.auth.server.rest.dto.request.ChangePasswordRequest;
import com.example.chat.auth.server.rest.dto.request.UpdateProfileRequest;
import com.example.chat.auth.server.rest.dto.response.ProfileResponse;
import com.example.chat.auth.server.profile.application.ProfileApplicationService;
import com.example.chat.auth.server.shared.security.CurrentPrincipalResolver;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.UUID;
@Slf4j
@RestController
@RequestMapping("/api/v1/me")
@RequiredArgsConstructor
public class ProfileApi{
private final ProfileApplicationService profileService;
private final CurrentPrincipalResolver principalResolver;
@GetMapping
public ResponseEntity<ProfileResponse> getMyProfile(HttpServletRequest r){UUID id=principalResolver.resolve(r);return ResponseEntity.ok(profileService.getProfile(id));}
@PatchMapping
public ResponseEntity<ProfileResponse> updateMyProfile(@Valid @RequestBody UpdateProfileRequest b,HttpServletRequest r){UUID id=principalResolver.resolve(r);return ResponseEntity.ok(profileService.updateProfile(id,b));}
@PatchMapping("/password")
public ResponseEntity<Void> changePassword(@Valid @RequestBody ChangePasswordRequest b,HttpServletRequest r){UUID id=principalResolver.resolve(r);profileService.changePassword(id,b);return ResponseEntity.noContent().build();}
}
