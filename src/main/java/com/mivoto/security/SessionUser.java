package com.mivoto.security;

public record SessionUser(String subject, String givenName, String familyName, String email) {

  public String displayName() {
    if (givenName != null && familyName != null) {
      return givenName + " " + familyName;
    }
    return givenName != null ? givenName : subject;
  }
}
