import { CommonModule } from '@angular/common';
import { Component, computed, effect, inject, input, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { CommentService } from '../../core/services/comment.service';
import { LikeService } from '../../core/services/like.service';
import { PostService } from '../../core/services/post.service';
import { AuthService } from '../../core/services/auth.service';
import { AvatarSharedComponent } from '../../shared/avatar-shared/avatar-shared.component';
import { ButtonSharedComponent } from '../../shared/button-shared/button-shared.component';
import { Comment } from '../../core/models/comment.model';
import { Post } from '../../core/models/post.model';

@Component({
  selector: 'app-post-detail',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink, AvatarSharedComponent, ButtonSharedComponent],
  templateUrl: './post-detail.component.html',
  styleUrl: './post-detail.component.scss'
})
export class PostDetailComponent {
  private router = inject(Router);
  private postService = inject(PostService);
  private commentService = inject(CommentService);
  private likeService = inject(LikeService);
  private authService = inject(AuthService);
  private fb = inject(FormBuilder);

  readonly id = input.required<string>();
  readonly postId = computed(() => {
    const parsed = Number(this.id());
    return Number.isFinite(parsed) && parsed > 0 ? parsed : 0;
  });

  readonly post = signal<Post | null>(null);
  readonly comments = signal<Comment[]>([]);
  readonly loading = signal(false);
  readonly errorMessage = signal<string | null>(null);
  readonly editing = signal(false);
  readonly mediaUrls = computed(() => {
    const raw = this.post()?.mediaUrls;
    if (!raw) {
      return [] as string[];
    }
    return raw.split(',').map((entry) => entry.trim()).filter(Boolean);
  });
  readonly canEdit = computed(() => {
    const post = this.post();
    const auth = this.authService.auth();
    return !!post && !!auth && post.userId === auth.userId;
  });
  readonly authorLink = computed(() => {
    const post = this.post();
    if (!post) {
      return ['/feed'];
    }
    const auth = this.authService.auth();
    const target = post.username?.trim();
    if (auth && (auth.userId === post.userId || auth.username?.trim() === target)) {
      const selfName = auth.username?.trim() ?? target;
      return selfName ? ['/u', selfName] : ['/feed'];
    }
    return target ? ['/u', target] : ['/feed'];
  });

  readonly commentForm = this.fb.nonNullable.group({
    content: ['', [Validators.required, Validators.maxLength(1000)]]
  });

  readonly editForm = this.fb.nonNullable.group({
    title: ['', [Validators.required, Validators.maxLength(100)]],
    content: ['', [Validators.required, Validators.maxLength(5000)]],
    mediaUrls: ['']
  });

  constructor() {
    effect(() => {
      const id = this.postId();
      if (!id) {
        this.post.set(null);
        this.comments.set([]);
        this.errorMessage.set('Post not found');
        this.loading.set(false);
        return;
      }
      this.loadPost(id);
    });
  }

  private loadPost(id: number) {
    this.loading.set(true);
    this.errorMessage.set(null);
    this.editing.set(false);
    this.post.set(null);
    this.comments.set([]);
    this.commentForm.reset({ content: '' });
    this.editForm.reset({ title: '', content: '', mediaUrls: '' });

    this.postService.getById(id).subscribe({
      next: (post) => {
        this.post.set(post);
        this.editForm.patchValue({
          title: post.title ?? '',
          content: post.content,
          mediaUrls: post.mediaUrls ?? ''
        });
        this.loading.set(false);
      },
      error: () => {
        this.errorMessage.set('Failed to load post');
        this.loading.set(false);
      }
    });

    this.commentService.getByPost(id).subscribe({
      next: (page) => this.comments.set(page.content ?? []),
      error: () => this.errorMessage.set('Failed to load comments')
    });
  }

  togglePostLike() {
    const post = this.post();
    if (!post) {
      return;
    }

    const action$ = post.likedByCurrentUser
      ? this.likeService.unlikePost(post.id)
      : this.likeService.likePost(post.id);

    action$.subscribe({
      next: () => {
        const liked = !post.likedByCurrentUser;
        const current = post.likeCount ?? 0;
        const likeCount = liked ? current + 1 : Math.max(0, current - 1);
        this.post.set({ ...post, likedByCurrentUser: liked, likeCount });
      },
      error: () => this.errorMessage.set('Failed to update like')
    });
  }

  commentAuthorLink(comment: Comment) {
    const username = comment.username?.trim();
    return username ? ['/u', username] : ['/feed'];
  }

  toggleEdit() {
    this.editing.set(!this.editing());
  }

  saveEdit() {
    const post = this.post();
    if (!post || this.editForm.invalid) {
      return;
    }

    const payload = this.editForm.getRawValue();
    this.postService.update(post.id, {
      title: payload.title,
      content: payload.content,
      mediaUrls: payload.mediaUrls || undefined
    }).subscribe({
      next: (updated) => {
        this.post.set(updated);
        this.editing.set(false);
      },
      error: () => this.errorMessage.set('Failed to update post')
    });
  }

  deletePost() {
    const post = this.post();
    if (!post) {
      return;
    }

    this.postService.delete(post.id).subscribe({
      next: () => {
        this.post.set(null);
        this.router.navigate(['/feed']);
      },
      error: () => this.errorMessage.set('Failed to delete post')
    });
  }

  toggleCommentLike(comment: Comment) {
    const action$ = comment.likedByCurrentUser
      ? this.likeService.unlikeComment(comment.id)
      : this.likeService.likeComment(comment.id);

    action$.subscribe({
      next: () => {
        const updated = this.comments().map((item) => {
          if (item.id !== comment.id) {
            return item;
          }
          const liked = !item.likedByCurrentUser;
          const current = item.likeCount ?? 0;
          const likeCount = liked ? current + 1 : Math.max(0, current - 1);
          return { ...item, likedByCurrentUser: liked, likeCount };
        });
        this.comments.set(updated);
      },
      error: () => this.errorMessage.set('Failed to update comment like')
    });
  }

  canEditComment(comment: Comment) {
    const auth = this.authService.auth();
    return !!auth && comment.userId === auth.userId;
  }

  deleteComment(comment: Comment) {
    this.commentService.delete(comment.id).subscribe({
      next: () => {
        this.comments.set(this.comments().filter((item) => item.id !== comment.id));
        const post = this.post();
        if (post) {
          const current = post.commentCount ?? 0;
          this.post.set({ ...post, commentCount: Math.max(0, current - 1) });
        }
      },
      error: () => this.errorMessage.set('Failed to delete comment')
    });
  }

  submitComment() {
    if (this.commentForm.invalid) {
      this.commentForm.markAllAsTouched();
      return;
    }

    const post = this.post();
    if (!post) {
      return;
    }

    const content = this.commentForm.getRawValue().content;
    this.commentService.create(post.id, { content }).subscribe({
      next: (comment) => {
        this.comments.set([comment, ...this.comments()]);
        this.commentForm.reset({ content: '' });
        const current = post.commentCount ?? 0;
        this.post.set({ ...post, commentCount: current + 1 });
      },
      error: () => this.errorMessage.set('Failed to add comment')
    });
  }
}
