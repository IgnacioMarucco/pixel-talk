export interface LikeResponse {
  id: number;
  userId: number;
  postId?: number;
  commentId?: number;
  createdAt?: string;
}
