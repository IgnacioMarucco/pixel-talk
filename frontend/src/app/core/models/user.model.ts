export interface User {
  id: number;
  username: string;
  email: string;
  firstName?: string;
  lastName?: string;
  profilePictureUrl?: string;
  bio?: string;
  createdAt?: string;
  updatedAt?: string;
}

export interface UserSummary {
  id: number;
  username: string;
  fullName?: string;
  profilePictureUrl?: string;
}

export interface UserUpdate {
  email?: string;
  firstName?: string;
  lastName?: string;
  profilePictureUrl?: string;
  bio?: string;
}

export interface ChangePasswordRequest {
  currentPassword: string;
  newPassword: string;
}
