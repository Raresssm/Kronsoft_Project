"use client";

import Image from "next/image";
import { FormEvent, useMemo, useState } from "react";
import { AppShell } from "../components/AppShell";

type FeedFilter = "All" | "Posts" | "Events";
type FeedKind = "Post" | "Event";

type FeedComment = {
  id: string;
  author: string;
  time: string;
  text: string;
  mine?: boolean;
};

type FeedPost = {
  id: string;
  author: string;
  role: string;
  avatar: string;
  time: string;
  kind: FeedKind;
  body: string;
  media?: string;
  tags: string[];
  likes: number;
  comments: FeedComment[];
  liked: boolean;
  saved: boolean;
};

const filters: FeedFilter[] = ["All", "Posts", "Events"];
const composerKinds: FeedKind[] = ["Post", "Event"];
const quickPicks = ["Campus events", "Design review", "Study group", "Project share"];

const initialPosts: FeedPost[] = [
  {
    id: "feed-1",
    author: "Agora Campus",
    role: "Community post",
    avatar: "/logo.png",
    time: "10 min ago",
    kind: "Post",
    body: "The student wall is live. Share one post or one event from this week.",
    media: "/agora_campus.jpg",
    tags: ["Campus", "Community"],
    likes: 34,
    comments: [
      {
        id: "c-1",
        author: "Mara Dobre",
        time: "8 min ago",
        text: "Great start. I will add a project update later today.",
      },
    ],
    liked: false,
    saved: true,
  },
  {
    id: "feed-2",
    author: "Mara Dobre",
    role: "Computer science student",
    avatar: "/agora.jpg",
    time: "35 min ago",
    kind: "Post",
    body: "I tightened the spacing and hierarchy on my dashboard. It scans much better on mobile now.",
    media: "/agora.jpg",
    tags: ["UI", "Responsive"],
    likes: 19,
    comments: [
      {
        id: "c-2",
        author: "You",
        time: "20 min ago",
        text: "The spacing reads much cleaner now.",
        mine: true,
      },
    ],
    liked: true,
    saved: false,
  },
  {
    id: "feed-3",
    author: "Design Guild",
    role: "Campus event",
    avatar: "/image.png",
    time: "Yesterday",
    kind: "Event",
    body: "Portfolio review night is on Friday. Bring one project and one question.",
    media: "/image.png",
    tags: ["Portfolio", "Mentoring"],
    likes: 48,
    comments: [],
    liked: false,
    saved: false,
  },
  {
    id: "feed-4",
    author: "Alex Radu",
    role: "Student developer",
    avatar: "/agora_campus.jpg",
    time: "Yesterday",
    kind: "Post",
    body: "For a class project, would you keep the state local until the UI settles?",
    tags: ["State", "Architecture"],
    likes: 12,
    comments: [
      {
        id: "c-3",
        author: "Ioana Pop",
        time: "Yesterday",
        text: "I would keep it local until the layout is stable.",
      },
    ],
    liked: false,
    saved: false,
  },
  {
    id: "feed-5",
    author: "Agora Campus",
    role: "Event board",
    avatar: "/logo.png",
    time: "2 days ago",
    kind: "Event",
    body: "Open mic night is Friday at 19:00. Bring a short performance or just come to watch.",
    tags: ["Music", "Community"],
    likes: 21,
    comments: [],
    liked: false,
    saved: false,
  },
];

export default function Feed() {
  const [posts, setPosts] = useState<FeedPost[]>(initialPosts);
  const [activeFilter, setActiveFilter] = useState<FeedFilter>("All");
  const [query, setQuery] = useState("");
  const [draft, setDraft] = useState("");
  const [draftKind, setDraftKind] = useState<FeedKind>("Post");
  const [openCommentsPostId, setOpenCommentsPostId] = useState<string | null>(null);
  const [commentDraft, setCommentDraft] = useState("");

  const normalizedQuery = query.trim().toLowerCase();

  const visiblePosts = useMemo(() => {
    return posts.filter((post) => {
      const matchesFilter =
        activeFilter === "All" ||
        (activeFilter === "Posts" && post.kind === "Post") ||
        (activeFilter === "Events" && post.kind === "Event");
      const matchesSearch =
        !normalizedQuery ||
        [post.author, post.role, post.body, post.kind, post.tags.join(" ")]
          .join(" ")
          .toLowerCase()
          .includes(normalizedQuery);

      return matchesFilter && matchesSearch;
    });
  }, [activeFilter, normalizedQuery, posts]);

  const eventHighlights = useMemo(
    () => posts.filter((post) => post.kind === "Event").slice(0, 3),
    [posts],
  );

  const handlePublish = (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();

    const body = draft.trim();
    if (!body) {
      return;
    }

    const nextPost: FeedPost = {
      id: `feed-${Date.now()}`,
      author: "You",
      role: "Agora Campus member",
      avatar: "/logo.png",
      time: "Just now",
      kind: draftKind,
      body,
      tags: body
        .split(/\s+/)
        .filter((word) => word.startsWith("#"))
        .map((word) => word.slice(1))
        .slice(0, 4),
      likes: 0,
      comments: [],
      liked: false,
      saved: false,
    };

    setPosts((currentPosts) => [nextPost, ...currentPosts]);
    setDraft("");
    setDraftKind("Post");
  };

  const toggleLike = (id: string) => {
    setPosts((currentPosts) =>
      currentPosts.map((post) =>
        post.id === id
          ? { ...post, liked: !post.liked, likes: post.liked ? post.likes - 1 : post.likes + 1 }
          : post,
      ),
    );
  };

  const toggleSaved = (id: string) => {
    setPosts((currentPosts) =>
      currentPosts.map((post) => (post.id === id ? { ...post, saved: !post.saved } : post)),
    );
  };

  const toggleComments = (id: string) => {
    setOpenCommentsPostId((currentId) => (currentId === id ? null : id));
    setCommentDraft("");
  };

  const handleCommentSubmit = (event: FormEvent<HTMLFormElement>, id: string) => {
    event.preventDefault();

    const text = commentDraft.trim();
    if (!text) {
      return;
    }

    setPosts((currentPosts) =>
      currentPosts.map((post) =>
        post.id === id
          ? {
              ...post,
              comments: [
                ...post.comments,
                {
                  id: `comment-${Date.now()}`,
                  author: "You",
                  time: "Just now",
                  text,
                  mine: true,
                },
              ],
            }
          : post,
      ),
    );

    setCommentDraft("");
  };

  return (
    <AppShell
      searchValue={query}
      onSearchChange={setQuery}
      searchPlaceholder="Search posts or events"
    >
      <div className="grid gap-6 lg:grid-cols-[280px_minmax(0,1fr)_260px]">
        <aside className="space-y-4 lg:sticky lg:top-24 lg:self-start">
          <section className="overflow-hidden rounded-3xl border border-white/20 bg-white/80 shadow-2xl backdrop-blur-xl">
            <div className="relative h-24">
              <Image src="/blur_cover.png" alt="" fill sizes="280px" className="object-cover" />
              <div className="absolute inset-0 bg-[#143b5d]/15" />
            </div>
            <div className="px-4 pb-4">
              <div className="relative -mt-9 h-18 w-18 overflow-hidden rounded-full border-4 border-white shadow-xl">
                <Image
                  src="/logo.png"
                  alt="Agora Campus"
                  fill
                  sizes="72px"
                  className="object-contain bg-white p-2"
                />
              </div>
              <h2 className="mt-3 text-lg font-semibold text-[#143b5d]">Agora Campus</h2>
              <p className="text-sm text-slate-700">A campus feed for posts and events only.</p>
              <dl className="mt-4 grid grid-cols-3 gap-2 border-t border-white/40 pt-4 text-center">
                <Stat label="Posts" value={posts.filter((post) => post.kind === "Post").length} />
                <Stat label="Events" value={posts.filter((post) => post.kind === "Event").length} />
                <Stat label="Saved" value={posts.filter((post) => post.saved).length} />
              </dl>
            </div>
          </section>

          <section className="rounded-3xl border border-white/20 bg-white/80 p-4 shadow-2xl backdrop-blur-xl">
            <h3 className="text-sm font-semibold text-[#143b5d]">Quick picks</h3>
            <div className="mt-3 flex flex-wrap gap-2">
              {quickPicks.map((pick) => (
                <button
                  key={pick}
                  type="button"
                  onClick={() => setQuery(pick)}
                  className="rounded-full bg-[#143b5d]/10 px-3 py-1.5 text-xs font-semibold text-[#143b5d] hover:bg-[#143b5d]/20"
                >
                  #{pick}
                </button>
              ))}
            </div>
          </section>
        </aside>

        <section className="min-w-0 space-y-4">
          <form
            onSubmit={handlePublish}
            className="overflow-hidden rounded-3xl border border-white/20 bg-white/80 shadow-2xl backdrop-blur-xl"
          >
            <div className="flex items-start gap-3 p-4 sm:p-5">
              <div className="relative h-12 w-12 shrink-0 overflow-hidden rounded-full border border-white/50 bg-white">
                <Image src="/logo.png" alt="Your avatar" fill sizes="48px" className="object-contain p-1" />
              </div>
              <div className="min-w-0 flex-1">
                <textarea
                  value={draft}
                  onChange={(event) => setDraft(event.target.value)}
                  placeholder="Share a post or event..."
                  className="min-h-24 w-full resize-none rounded-2xl border border-white/40 bg-white/80 px-4 py-3 text-sm text-slate-800 outline-none focus:border-[#143b5d] focus:ring-4 focus:ring-[#143b5d]/10"
                />

                <div className="mt-3 flex flex-wrap items-center gap-2">
                  {composerKinds.map((kind) => (
                    <button
                      key={kind}
                      type="button"
                      onClick={() => setDraftKind(kind)}
                      className={[
                        "rounded-full px-3 py-1.5 text-xs font-semibold transition",
                        draftKind === kind
                          ? "bg-[#143b5d] text-white"
                          : "bg-white text-[#143b5d] hover:bg-[#143b5d]/10",
                      ].join(" ")}
                    >
                      {kind}
                    </button>
                  ))}
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

          <div className="flex flex-wrap items-center gap-2 rounded-3xl border border-white/20 bg-white/80 p-3 shadow-2xl backdrop-blur-xl">
            {filters.map((filter) => (
              <button
                key={filter}
                type="button"
                onClick={() => setActiveFilter(filter)}
                className={[
                  "rounded-full px-4 py-2 text-sm font-semibold transition",
                  activeFilter === filter
                    ? "bg-[#143b5d] text-white"
                    : "bg-white text-[#143b5d] hover:bg-[#143b5d]/10",
                ].join(" ")}
              >
                {filter}
              </button>
            ))}
            <span className="ml-auto text-xs font-medium text-slate-600">
              {visiblePosts.length} items visible
            </span>
          </div>

          <div className="space-y-4">
            {visiblePosts.map((post) => {
              const commentsOpen = openCommentsPostId === post.id;

              return (
                <article
                  key={post.id}
                  className="overflow-hidden rounded-3xl border border-white/20 bg-white/80 shadow-2xl backdrop-blur-xl"
                >
                  {post.media && (
                    <div className="relative h-52">
                      <Image
                        src={post.media}
                        alt=""
                        fill
                        sizes="(max-width: 1024px) 100vw, 700px"
                        className="object-cover"
                      />
                      <div className="absolute inset-0 bg-gradient-to-t from-black/15 to-transparent" />
                    </div>
                  )}

                  <div className="p-4 sm:p-5">
                    <header className="flex min-w-0 items-start gap-3">
                      <div className="relative h-12 w-12 shrink-0 overflow-hidden rounded-full border border-white/50 bg-white">
                        <Image src={post.avatar} alt={post.author} fill sizes="48px" className="object-cover" />
                      </div>
                      <div className="min-w-0 flex-1">
                        <div className="flex flex-wrap items-center gap-2">
                          <h3 className="truncate font-semibold text-[#143b5d]">{post.author}</h3>
                          <span className="rounded-full bg-[#143b5d]/10 px-2.5 py-1 text-[11px] font-semibold uppercase tracking-wide text-[#143b5d]">
                            {post.kind}
                          </span>
                        </div>
                        <p className="text-sm text-slate-700">{post.role}</p>
                        <p className="text-xs text-slate-500">{post.time}</p>
                      </div>
                    </header>

                    <p className="mt-4 text-sm leading-6 text-slate-800">{post.body}</p>

                    <div className="mt-4 flex flex-wrap gap-2">
                      {post.tags.map((tag) => (
                        <span
                          key={`${post.id}-${tag}`}
                          className="rounded-full bg-slate-100 px-2.5 py-1 text-xs font-medium text-slate-700"
                        >
                          #{tag}
                        </span>
                      ))}
                    </div>

                    <footer className="mt-4 flex flex-wrap items-center gap-2 border-t border-white/40 pt-3">
                      <button
                        type="button"
                        onClick={() => toggleLike(post.id)}
                        className={[
                          "rounded-full px-4 py-2 text-sm font-semibold transition",
                          post.liked
                            ? "bg-[#143b5d] text-white"
                            : "bg-white text-[#143b5d] hover:bg-[#143b5d]/10",
                        ].join(" ")}
                      >
                        {post.likes} Likes
                      </button>
                      <button
                        type="button"
                        onClick={() => toggleComments(post.id)}
                        className={[
                          "rounded-full px-4 py-2 text-sm font-semibold transition",
                          commentsOpen
                            ? "bg-[#143b5d] text-white"
                            : "bg-white text-[#143b5d] hover:bg-[#143b5d]/10",
                        ].join(" ")}
                      >
                        {post.comments.length} Comments
                      </button>
                      <button
                        type="button"
                        onClick={() => toggleSaved(post.id)}
                        className={[
                          "rounded-full px-4 py-2 text-sm font-semibold transition",
                          post.saved
                            ? "bg-amber-100 text-amber-700"
                            : "bg-white text-[#143b5d] hover:bg-[#143b5d]/10",
                        ].join(" ")}
                      >
                        {post.saved ? "Saved" : "Save"}
                      </button>
                    </footer>

                    {commentsOpen && (
                      <section className="mt-4 rounded-2xl border border-slate-200 bg-white/90 p-4">
                        <div className="space-y-3">
                          {post.comments.length > 0 ? (
                            post.comments.map((comment) => (
                              <div key={comment.id} className="rounded-xl bg-slate-50 px-3 py-2">
                                <div className="flex items-center justify-between gap-2">
                                  <p className="text-sm font-semibold text-slate-700">
                                    {comment.author}
                                  </p>
                                  <span className="text-[11px] text-slate-500">{comment.time}</span>
                                </div>
                                <p className="mt-1 text-sm text-slate-700">{comment.text}</p>
                              </div>
                            ))
                          ) : (
                            <p className="text-sm text-slate-500">No comments yet.</p>
                          )}
                        </div>

                        <form
                          className="mt-4 flex flex-col gap-2 sm:flex-row"
                          onSubmit={(event) => handleCommentSubmit(event, post.id)}
                        >
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

            {visiblePosts.length === 0 && (
              <section className="rounded-3xl border border-dashed border-white/30 bg-white/70 px-6 py-12 text-center shadow-2xl backdrop-blur-xl">
                <h2 className="font-semibold text-[#143b5d]">No posts found</h2>
                <p className="mt-1 text-sm text-slate-600">Try another search term or category.</p>
              </section>
            )}
          </div>
        </section>

        <aside className="space-y-4 lg:sticky lg:top-24 lg:self-start">
          <section className="rounded-3xl border border-white/20 bg-white/80 p-4 shadow-2xl backdrop-blur-xl">
            <h3 className="text-sm font-semibold text-[#143b5d]">Events</h3>
            <div className="mt-3 space-y-3 text-sm">
              {eventHighlights.length > 0 ? (
                eventHighlights.map((event) => (
                  <div key={event.id} className="border-b border-white/50 pb-3 last:border-0 last:pb-0">
                    <p className="font-medium text-slate-800">{event.body}</p>
                    <p className="text-slate-500">
                      {event.author} · {event.time}
                    </p>
                  </div>
                ))
              ) : (
                <p className="text-slate-600">No events yet.</p>
              )}
            </div>
          </section>
        </aside>
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
