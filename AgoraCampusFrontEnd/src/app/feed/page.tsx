"use client";

import Image from "next/image";
import { FormEvent, useCallback, useEffect, useMemo, useState } from "react";
import { AppShell } from "../components/AppShell";
import { CommentResponse, formatRelativeTime, PostResponse, ReactionResponse } from "../lib/api-types";
import { useAuth } from "../lib/auth";

type FeedItem = {
  post: PostResponse;
  comments: CommentResponse[];
  reactions: ReactionResponse[];
};

export default function Feed() {
  const { appUser, apiFetch } = useAuth();
  const [items, setItems] = useState<FeedItem[]>([]);
  const [users, setUsers] = useState<Record<number, string>>({});
  const [query, setQuery] = useState("");
  const [draft, setDraft] = useState("");
  const [openCommentsPostId, setOpenCommentsPostId] = useState<number | null>(null);
  const [commentDraft, setCommentDraft] = useState("");
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  const loadFeed = useCallback(async () => {
    if (!appUser) return;

    setLoading(true);
    setError("");

    try {
      const [usersResponse, postsResponse] = await Promise.all([
        apiFetch("/api/users"),
        apiFetch("/api/posts"),
      ]);

      if (!usersResponse.ok) throw new Error(`Could not load users (${usersResponse.status}).`);
      if (!postsResponse.ok) throw new Error(`Could not load posts (${postsResponse.status}).`);

      const userList = (await usersResponse.json()) as Array<{ id: number; displayName?: string; username: string }>;
      const userMap = Object.fromEntries(userList.map((user) => [user.id, user.displayName || user.username]));
      const posts = (await postsResponse.json()) as PostResponse[];

      const nextItems = await Promise.all(
        posts.map(async (post) => {
          const [commentsResponse, reactionsResponse] = await Promise.all([
            apiFetch(`/api/posts/${post.postId}/comments`),
            apiFetch(`/api/posts/${post.postId}/reactions`),
          ]);

          return {
            post,
            comments: commentsResponse.ok ? ((await commentsResponse.json()) as CommentResponse[]) : [],
            reactions: reactionsResponse.ok ? ((await reactionsResponse.json()) as ReactionResponse[]) : [],
          };
        }),
      );

      setUsers(userMap);
      setItems(nextItems);
    } catch (loadError) {
      setError(loadError instanceof Error ? loadError.message : "Could not load feed.");
    } finally {
      setLoading(false);
    }
  }, [apiFetch, appUser]);

  useEffect(() => {
    queueMicrotask(() => void loadFeed());
  }, [loadFeed]);

  const normalizedQuery = query.trim().toLowerCase();
  const visibleItems = useMemo(() => {
    return items.filter(({ post }) => {
      const author = users[post.authorUserId] ?? "Unknown";
      return (
        !normalizedQuery ||
        [author, post.content].join(" ").toLowerCase().includes(normalizedQuery)
      );
    });
  }, [items, normalizedQuery, users]);

  const handlePublish = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    if (!appUser) return;

    const content = draft.trim();
    if (!content) return;

    const response = await apiFetch(`/api/posts?actingUserId=${appUser.id}`, {
      method: "POST",
      body: JSON.stringify({ authorUserId: appUser.id, content, mediaUrl: null }),
    });

    if (!response.ok) {
      setError(`Could not publish post (${response.status}).`);
      return;
    }

    setDraft("");
    await loadFeed();
  };

  const toggleLike = async (item: FeedItem) => {
    if (!appUser) return;

    const myReaction = item.reactions.find((reaction) => reaction.authorUserId === appUser.id);
    const path = `/api/posts/${item.post.postId}/reactions?authorUserId=${appUser.id}&actingUserId=${appUser.id}`;
    const response = myReaction
      ? await apiFetch(path, { method: "DELETE" })
      : await apiFetch(`/api/posts/${item.post.postId}/reactions?actingUserId=${appUser.id}`, {
          method: "PUT",
          body: JSON.stringify({ authorUserId: appUser.id, reactionType: "LIKE" }),
        });

    if (!response.ok) {
      setError(`Could not update reaction (${response.status}).`);
      return;
    }

    await loadFeed();
  };

  const handleCommentSubmit = async (event: FormEvent<HTMLFormElement>, postId: number) => {
    event.preventDefault();
    if (!appUser) return;

    const content = commentDraft.trim();
    if (!content) return;

    const response = await apiFetch(`/api/posts/${postId}/comments?actingUserId=${appUser.id}`, {
      method: "POST",
      body: JSON.stringify({ authorUserId: appUser.id, content }),
    });

    if (!response.ok) {
      setError(`Could not add comment (${response.status}).`);
      return;
    }

    setCommentDraft("");
    await loadFeed();
  };

  return (
    <AppShell searchValue={query} onSearchChange={setQuery} searchPlaceholder="Search posts">
      <div className="grid gap-6 lg:grid-cols-[280px_minmax(0,1fr)]">
        <aside className="space-y-4 lg:sticky lg:top-24 lg:self-start">
          <section className="overflow-hidden rounded-3xl border border-white/20 bg-white/80 shadow-2xl backdrop-blur-xl">
            <div className="relative h-24">
              <Image src="/blur_cover.png" alt="" fill sizes="280px" className="object-cover" />
              <div className="absolute inset-0 bg-[#143b5d]/15" />
            </div>
            <div className="px-4 pb-4">
              <div className="relative -mt-9 h-18 w-18 overflow-hidden rounded-full border-4 border-white shadow-xl">
                <Image src="/logo.png" alt="Agora Campus" fill sizes="72px" className="object-contain bg-white p-2" />
              </div>
              <h2 className="mt-3 text-lg font-semibold text-[#143b5d]">Agora Campus</h2>
              <p className="text-sm text-slate-700">Share updates with your campus network.</p>
              <dl className="mt-4 grid grid-cols-3 gap-2 border-t border-white/40 pt-4 text-center">
                <Stat label="Posts" value={items.length} />
                <Stat label="Likes" value={items.reduce((sum, item) => sum + item.reactions.length, 0)} />
                <Stat label="Comments" value={items.reduce((sum, item) => sum + item.comments.length, 0)} />
              </dl>
            </div>
          </section>
        </aside>

        <section className="min-w-0 space-y-4">
          <form onSubmit={handlePublish} className="overflow-hidden rounded-3xl border border-white/20 bg-white/80 shadow-2xl backdrop-blur-xl">
            <div className="flex items-start gap-3 p-4 sm:p-5">
              <div className="relative h-12 w-12 shrink-0 overflow-hidden rounded-full border border-white/50 bg-white">
                <Image src="/logo.png" alt="Your avatar" fill sizes="48px" className="object-contain p-1" />
              </div>
              <div className="min-w-0 flex-1">
                <textarea
                  value={draft}
                  onChange={(event) => setDraft(event.target.value)}
                  placeholder="Share a post..."
                  className="min-h-24 w-full resize-none rounded-2xl border border-white/40 bg-white/80 px-4 py-3 text-sm text-slate-800 outline-none focus:border-[#143b5d] focus:ring-4 focus:ring-[#143b5d]/10"
                />
                <div className="mt-3 flex items-center gap-2">
                  <button
                    type="submit"
                    disabled={!draft.trim()}
                    className="ml-auto rounded-full bg-[#143b5d] px-4 py-2 text-xs font-bold tracking-[0.2em] text-white transition hover:bg-[#1d5485] disabled:cursor-not-allowed disabled:opacity-50"
                  >
                    POST
                  </button>
                </div>
              </div>
            </div>
          </form>

          <div className="flex flex-wrap items-center gap-3 rounded-3xl border border-white/20 bg-white/80 p-3 shadow-2xl backdrop-blur-xl">
            <span className="rounded-full bg-[#143b5d] px-4 py-2 text-sm font-semibold text-white">Posts</span>
            <span className="ml-auto text-xs font-medium text-slate-600">
              {loading ? "Loading..." : `${visibleItems.length} items visible`}
            </span>
          </div>

          {error && <p className="rounded-2xl bg-red-50 px-4 py-3 text-sm text-red-600">{error}</p>}

          <div className="space-y-4">
            {visibleItems.map((item) => {
              const commentsOpen = openCommentsPostId === item.post.postId;
              const author = users[item.post.authorUserId] ?? "Unknown user";
              const liked = Boolean(appUser && item.reactions.some((reaction) => reaction.authorUserId === appUser.id));

              return (
                <article key={item.post.postId} className="overflow-hidden rounded-3xl border border-white/20 bg-white/80 shadow-2xl backdrop-blur-xl">
                  <div className="p-4 sm:p-5">
                    <header className="flex min-w-0 items-start gap-3">
                      <div className="relative h-12 w-12 shrink-0 overflow-hidden rounded-full border border-white/50 bg-white">
                        <Image src="/logo.png" alt={author} fill sizes="48px" className="object-contain p-1" />
                      </div>
                      <div className="min-w-0 flex-1">
                        <h3 className="truncate font-semibold text-[#143b5d]">{author}</h3>
                        <p className="text-sm text-slate-700">Agora Campus member</p>
                        <p className="text-xs text-slate-500">{formatRelativeTime(item.post.createdAt)}</p>
                      </div>
                    </header>

                    <p className="mt-4 whitespace-pre-wrap text-sm leading-6 text-slate-800">{item.post.content}</p>

                    <footer className="mt-4 flex flex-wrap items-center gap-2 border-t border-white/40 pt-3">
                      <button
                        type="button"
                        onClick={() => toggleLike(item)}
                        className={[
                          "rounded-full px-4 py-2 text-sm font-semibold transition",
                          liked ? "bg-[#143b5d] text-white" : "bg-white text-[#143b5d] hover:bg-[#143b5d]/10",
                        ].join(" ")}
                      >
                        {item.reactions.length} Likes
                      </button>
                      <button
                        type="button"
                        onClick={() => {
                          setOpenCommentsPostId((currentId) => (currentId === item.post.postId ? null : item.post.postId));
                          setCommentDraft("");
                        }}
                        className={[
                          "rounded-full px-4 py-2 text-sm font-semibold transition",
                          commentsOpen ? "bg-[#143b5d] text-white" : "bg-white text-[#143b5d] hover:bg-[#143b5d]/10",
                        ].join(" ")}
                      >
                        {item.comments.length} Comments
                      </button>
                    </footer>

                    {commentsOpen && (
                      <section className="mt-4 rounded-2xl border border-slate-200 bg-white/90 p-4">
                        <div className="space-y-3">
                          {item.comments.length > 0 ? (
                            item.comments.map((comment) => (
                              <div key={comment.commentId} className="rounded-xl bg-slate-50 px-3 py-2">
                                <div className="flex items-center justify-between gap-2">
                                  <p className="text-sm font-semibold text-slate-700">
                                    {users[comment.authorUserId] ?? "Unknown user"}
                                  </p>
                                  <span className="text-[11px] text-slate-500">{formatRelativeTime(comment.createdAt)}</span>
                                </div>
                                <p className="mt-1 text-sm text-slate-700">{comment.content}</p>
                              </div>
                            ))
                          ) : (
                            <p className="text-sm text-slate-500">No comments yet.</p>
                          )}
                        </div>

                        <form className="mt-4 flex flex-col gap-2 sm:flex-row" onSubmit={(event) => handleCommentSubmit(event, item.post.postId)}>
                          <input
                            type="text"
                            value={commentDraft}
                            onChange={(event) => setCommentDraft(event.target.value)}
                            placeholder="Write a comment..."
                            className="h-11 min-w-0 flex-1 rounded-2xl border border-slate-200 px-4 text-sm text-slate-800 outline-none focus:border-[#143b5d] focus:ring-4 focus:ring-[#143b5d]/10"
                          />
                          <button
                            type="submit"
                            disabled={!commentDraft.trim()}
                            className="h-11 rounded-2xl bg-[#143b5d] px-4 text-sm font-semibold text-white transition hover:bg-[#1d5485] disabled:cursor-not-allowed disabled:opacity-50"
                          >
                            Comment
                          </button>
                        </form>
                      </section>
                    )}
                  </div>
                </article>
              );
            })}

            {!loading && visibleItems.length === 0 && (
              <section className="rounded-3xl border border-dashed border-white/30 bg-white/70 px-6 py-12 text-center shadow-2xl backdrop-blur-xl">
                <h2 className="font-semibold text-[#143b5d]">No posts found</h2>
                <p className="mt-1 text-sm text-slate-600">Create the first post or try another search term.</p>
              </section>
            )}
          </div>
        </section>
      </div>
    </AppShell>
  );
}

function Stat({ label, value }: { label: string; value: number }) {
  return (
    <div>
      <dt className="text-[11px] uppercase tracking-wide text-slate-500">{label}</dt>
      <dd className="text-lg font-semibold text-[#143b5d]">{value}</dd>
    </div>
  );
}
