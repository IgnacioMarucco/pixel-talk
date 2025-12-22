import { CommonModule } from '@angular/common';
import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { concatMap, from, map, of, switchMap, toArray } from 'rxjs';
import { MediaService } from '../../core/services/media.service';
import { PostService } from '../../core/services/post.service';
import { PostSummary } from '../../core/models/post.model';
import { AuthService } from '../../core/services/auth.service';
import { AvatarSharedComponent } from '../../shared/avatar-shared/avatar-shared.component';
import { ButtonSharedComponent } from '../../shared/button-shared/button-shared.component';

@Component({
  selector: 'app-feed',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    RouterLink,
    AvatarSharedComponent,
    ButtonSharedComponent
  ],
  templateUrl: './feed.component.html',
  styleUrl: './feed.component.scss'
})
export class FeedComponent {
  private route = inject(ActivatedRoute);
  private fb = inject(FormBuilder);
  private postService = inject(PostService);
  private mediaService = inject(MediaService);
  private authService = inject(AuthService);

  readonly posts = signal<PostSummary[]>([]);
  readonly loading = signal(false);
  readonly errorMessage = signal<string | null>(null);
  readonly mode = signal<'feed' | 'global' | 'trending' | 'search'>('feed');
  readonly searchQuery = signal('');

  readonly form = this.fb.nonNullable.group({
    title: ['', [Validators.required, Validators.maxLength(100)]],
    content: ['', [Validators.required, Validators.maxLength(5000)]]
  });

  selectedFiles: File[] = [];

  constructor() {
    const mode = this.route.snapshot.data['mode'] as 'feed' | 'global' | 'trending' | undefined;
    if (mode === 'global') {
      this.loadGlobal();
      return;
    }
    if (mode === 'trending') {
      this.loadTrending();
      return;
    }
    this.loadFeed();
  }

  loadFeed() {
    this.mode.set('feed');
    this.searchQuery.set('');
    this.errorMessage.set(null);
    this.postService.getFeed().subscribe({
      next: (page) => {
        this.posts.set(page.content ?? []);
      },
      error: () => {
        this.errorMessage.set('Failed to load feed');
      }
    });
  }

  loadGlobal() {
    this.mode.set('global');
    this.searchQuery.set('');
    this.errorMessage.set(null);
    this.postService.getGlobal().subscribe({
      next: (page) => {
        this.posts.set(page.content ?? []);
      },
      error: () => {
        this.errorMessage.set('Failed to load global posts');
      }
    });
  }

  loadTrending() {
    this.mode.set('trending');
    this.searchQuery.set('');
    this.errorMessage.set(null);
    this.postService.getTrending().subscribe({
      next: (page) => this.posts.set(page.content ?? []),
      error: () => this.errorMessage.set('Failed to load trending')
    });
  }

  submitSearch() {
    const query = this.searchQuery().trim();
    if (!query) {
      this.loadFeed();
      return;
    }
    this.mode.set('search');
    this.errorMessage.set(null);
    this.postService.search(query).subscribe({
      next: (page) => this.posts.set(page.content ?? []),
      error: () => this.errorMessage.set('Failed to search posts')
    });
  }

  authorLink(username: string | undefined, userId: number) {
    const auth = this.authService.auth();
    const target = username?.trim();
    if (auth && (auth.userId === userId || auth.username?.trim() === target)) {
      const selfName = auth.username?.trim() ?? target;
      return selfName ? ['/u', selfName] : ['/feed'];
    }
    return target ? ['/u', target] : ['/feed'];
  }

  onFileChange(event: Event) {
    const input = event.target as HTMLInputElement;
    if (!input.files) {
      this.selectedFiles = [];
      return;
    }
    this.selectedFiles = Array.from(input.files);
  }

  submitPost() {
    if (this.form.invalid || this.loading()) {
      this.form.markAllAsTouched();
      return;
    }

    const { title, content } = this.form.getRawValue();
    const files = this.selectedFiles;

    this.loading.set(true);
    this.errorMessage.set(null);

    const upload$ = files.length
      ? from(files).pipe(
          concatMap((file) =>
            this.mediaService
              .createPresignedUpload({
                originalFilename: file.name,
                mimeType: file.type,
                fileSize: file.size
              })
              .pipe(
                switchMap((presigned) =>
                  this.mediaService.uploadToPresignedUrl(presigned.uploadUrl, file).pipe(
                    switchMap(() =>
                      this.mediaService.confirmPresignedUpload({
                        storedFilename: presigned.objectKey,
                        originalFilename: file.name,
                        mimeType: file.type,
                        fileSize: file.size
                      })
                    ),
                    map((confirmed) => confirmed.url)
                  )
                )
              )
          ),
          toArray()
        )
      : of([] as string[]);

    upload$
      .pipe(
        switchMap((urls) =>
          this.postService.create({
            title,
            content,
            mediaUrls: urls.length ? urls.join(',') : undefined
          })
        )
      )
      .subscribe({
        next: () => {
          this.form.reset({ title: '', content: '' });
          this.selectedFiles = [];
          this.loading.set(false);
          this.loadFeed();
        },
        error: (err) => {
          const detail = err?.error?.detail ?? 'Failed to create post';
          this.errorMessage.set(detail);
          this.loading.set(false);
        }
      });
  }
}
