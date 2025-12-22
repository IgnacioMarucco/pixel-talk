export interface PostSummary {
  id: number;
  userId: number;
  username?: string;
  profilePictureUrl?: string;
  title: string;
  likeCount: number;
  commentCount: number;
  createdAt: string;
}

export interface Post {
  id: number;
  userId: number;
  username?: string;
  authorFullName?: string;
  profilePictureUrl?: string;
  title: string;
  content: string;
  mediaUrls?: string;
  likeCount: number;
  commentCount: number;
  likedByCurrentUser?: boolean;
  createdAt: string;
  updatedAt?: string;
}

export interface PostCreate {
  title: string;
  content: string;
  mediaUrls?: string;
}

export interface PostUpdate {
  title?: string;
  content?: string;
  mediaUrls?: string;
}
