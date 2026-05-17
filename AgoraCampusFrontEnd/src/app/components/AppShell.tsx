"use client";

import Image from "next/image";
import Link from "next/link";
import { usePathname } from "next/navigation";
import type { ReactNode, SVGProps } from "react";
import { useAuth } from "@/contexts/AuthContext";

type IconProps = SVGProps<SVGSVGElement>;

type NavItem = {
  href: string;
  label: string;
  icon: (props: IconProps) => ReactNode;
};

type AppShellProps = {
  children: ReactNode;
  searchPlaceholder?: string;
  searchValue?: string;
  onSearchChange?: (value: string) => void;
};

const navItems: NavItem[] = [
  { href: "/feed", label: "Home", icon: HomeIcon },
  { href: "/messages", label: "Messages", icon: MessageIcon },
  { href: "/jobs", label: "Jobs", icon: BriefcaseIcon },
  { href: "/alerts", label: "Alerts", icon: BellIcon },
  { href: "/network", label: "Network", icon: NetworkIcon },
  { href: "/profile", label: "Profile", icon: ProfileIcon },
];

export function AppShell({
  children,
  searchPlaceholder = "Search Agora Campus",
  searchValue,
  onSearchChange,
}: AppShellProps) {
  const pathname = usePathname();
  const { appUser, logout } = useAuth();

  return (
    <div className="relative isolate min-h-screen overflow-x-hidden bg-[#3c3834] text-slate-900">
      <div className="fixed inset-0 z-0">
        <Image
          src="/blur_cover.png"
          alt=""
          fill
          sizes="100vw"
          className="object-cover"
          priority
        />
        <div className="absolute inset-0 bg-black/5" />
      </div>

      <header className="sticky top-0 z-50 border-b border-white/25 bg-white/20 shadow-2xl backdrop-blur-xl">
        <div className="mx-auto flex min-h-14 w-full max-w-7xl flex-wrap items-center gap-3 px-4 py-2 sm:px-6 lg:flex-nowrap lg:px-8">
          <Link
            href="/feed"
            aria-label="Agora Campus home"
            className="flex h-11 w-28 shrink-0 items-center"
          >
            <span className="relative block h-full w-full">
              <Image
                src="/logo.png"
                alt="Agora Campus"
                fill
                sizes="112px"
                className="object-contain object-left"
                priority
              />
            </span>
          </Link>

          <label className="order-3 flex h-11 w-full min-w-0 items-center gap-2 rounded-full border border-white/35 bg-white/30 px-4 text-[#143b5d] shadow-sm sm:max-w-md lg:order-none lg:w-[320px]">
            <SearchIcon className="h-5 w-5 shrink-0" />
            <input
              type="search"
              value={searchValue ?? ""}
              onChange={(event) => onSearchChange?.(event.target.value)}
              readOnly={!onSearchChange}
              placeholder={searchPlaceholder}
              className="min-w-0 flex-1 bg-transparent text-sm text-slate-900 outline-none placeholder:text-[#143b5d]/70"
            />
          </label>

          <div className="order-2 ml-auto flex items-center gap-2">
            {appUser && (
              <span className="hidden max-w-[140px] truncate text-xs font-medium text-[#143b5d] sm:inline">
                {appUser.username}
              </span>
            )}
            <button
              type="button"
              onClick={() => void logout()}
              className="rounded-full border border-[#143b5d]/30 px-3 py-1.5 text-[11px] font-semibold uppercase tracking-wide text-[#143b5d] transition hover:bg-white/30"
            >
              Sign out
            </button>
          </div>

          <nav className="order-3 flex max-w-full items-center gap-1 overflow-x-auto pb-1 lg:order-none lg:pb-0">
            {navItems.map((item) => {
              const isActive = pathname === item.href;

              return (
                <Link
                  key={item.href}
                  href={item.href}
                  className={[
                    "flex h-11 min-w-[74px] shrink-0 flex-col items-center justify-center gap-0.5 rounded-xl px-2 text-[11px] font-semibold transition sm:min-w-0 sm:px-3",
                    isActive
                      ? "bg-white/35 text-[#143b5d] shadow-sm"
                      : "text-[#143b5d] hover:bg-white/15",
                  ].join(" ")}
                >
                  {item.icon({ className: "h-5 w-5" })}
                  <span className="leading-none">{item.label}</span>
                </Link>
              );
            })}
          </nav>
        </div>
      </header>

      <main className="relative z-10 mx-auto w-full max-w-7xl px-4 py-5 sm:px-6 lg:px-8">
        {children}
      </main>
    </div>
  );
}

function SearchIcon(props: IconProps) {
  return (
    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" {...props}>
      <circle cx="11" cy="11" r="7" />
      <line x1="16.65" y1="16.65" x2="21" y2="21" />
    </svg>
  );
}

function HomeIcon(props: IconProps) {
  return (
    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" {...props}>
      <path d="M3 10.5 12 3l9 7.5" />
      <path d="M5 9.5V21h14V9.5" />
      <path d="M10 21v-6h4v6" />
    </svg>
  );
}

function MessageIcon(props: IconProps) {
  return (
    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" {...props}>
      <path d="M21 15a4 4 0 0 1-4 4H8l-5 3 2-4a4 4 0 0 1-2-3V7a4 4 0 0 1 4-4h10a4 4 0 0 1 4 4z" />
    </svg>
  );
}

function BriefcaseIcon(props: IconProps) {
  return (
    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" {...props}>
      <rect x="3" y="7" width="18" height="14" rx="2" />
      <path d="M8 7V5a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2" />
      <path d="M3 13h18" />
    </svg>
  );
}

function BellIcon(props: IconProps) {
  return (
    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" {...props}>
      <path d="M18 8a6 6 0 0 0-12 0c0 7-3 7-3 7h18s-3 0-3-7" />
      <path d="M13.73 21a2 2 0 0 1-3.46 0" />
    </svg>
  );
}

function NetworkIcon(props: IconProps) {
  return (
    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" {...props}>
      <circle cx="9" cy="8" r="3" />
      <path d="M3 20c0-3.3 2.7-6 6-6" />
      <circle cx="17" cy="10" r="3" />
      <path d="M13 20c0-3.3 2.7-6 6-6" />
    </svg>
  );
}

function ProfileIcon(props: IconProps) {
  return (
    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" {...props}>
      <circle cx="12" cy="12" r="10" />
      <circle cx="12" cy="10" r="3" />
      <path d="M8 18c0-2.2 1.8-4 4-4s4 1.8 4 4" />
    </svg>
  );
}
