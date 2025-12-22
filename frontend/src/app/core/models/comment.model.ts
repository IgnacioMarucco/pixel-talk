export interface Comment {
  id: number;
  postId: number;
  userId: number;
  username?: string;
  authorFullName?: string;
  content: string;
  parentCommentId?: number;
  likeCount: number;
  likedByCurrentUser?: boolean;
  createdAt: string;
  updatedAt?: string;
}

export interface CommentCreate {
  content: string;
  parentCommentId?: number;
}

export interface CommentUpdate {
  content: string;
}
