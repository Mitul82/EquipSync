import { useQueryClient } from '@tanstack/react-query';
import { LogOut, X } from 'lucide-react';
import React from 'react';
import toast from 'react-hot-toast';
import { Link, useLocation, useNavigate } from 'react-router-dom';

import api from '@/utils/api';
import useAuth from '@/hooks/useAuth';
import logo from '@/assets/equipsync.svg';

import type { NavSection } from '@/components/navConfig';

interface SidebarProps {
    sections: NavSection[],
    isMobileOpen: boolean,
    setIsMobileOpen: (open: boolean) => void
}

const matches = (pathname: string, href: string) => pathname === href || pathname.startsWith(`${href}/`);

/*
    * NESTED ACTIVE LINK PROBLEM

    * Problem: isActive() uses a prefix match (pathname.startsWith(`${href}/`)), so a
    * parent route stays highlighted on its child routes. For example,
    * '/dashboard/assets/available' starts with '/dashboard/assets/', which made both
    * "All Assets" and "Available Assets" look active at the same time.

    * Fix: give the parent item `end: true`. isActive() then does an exact match for
    * that item, so it only lights up on its own route. Items without `end` still use
    * the prefix match, so their child routes (e.g. a detail page) keep them active.

    * Rule of thumb: add `end: true` to any nav item whose href is a prefix of
    * another nav item's href ('/dashboard' and '/dashboard/assets' both qualify).

    * Trade-off: with `end: true`, the parent no longer highlights on deeper routes
    * like '/dashboard/assets/123'. If that's needed later, drop `end` on that item
    * and explicitly exclude the sibling route in isActive() instead.

    * Same behavior as the `end` prop on react-router's <NavLink>.
*/

/* 
    ? const navItems = [
    ?     { label: 'Dashboard', href: '/dashboard', icon: LayoutDashboard, end: true },
    ?     { label: 'All Assets', href: '/dashboard/assets', icon: Monitor, end: true },
    ?     { label: 'Available Assets', href: '/dashboard/assets/available', icon: CheckCircle },
    ?     { label: 'My Requests', href: '/dashboard/requests', icon: ClipboardList, end: true },
    ?     { label: 'Raise Request', href: '/dashboard/requests/create', icon: GitPullRequestCreateArrow, end: true }
    ? ]
*/

/*
    * HOW ACTIVE LINK STATUS WORKS (longest-match approach)

    * The Sidebar now computes a single `activeHref` and each link is active only if
    * `href === activeHref`.

    * How activeHref is computed:
    *   1. Take every nav item the current user's role can see.
    *   2. Keep the ones where matches(pathname, href) is true, meaning the pathname equals the href or starts with `${href}/`.
    *   3. Sort those by href length, longest first, and pick the first one.

    * Example: on '/dashboard/assets/available', both '/dashboard/assets' and
    * '/dashboard/assets/available' match, but the longer one wins, so only
    * "Available Assets" is highlighted. The same rule makes '/dashboard/requests/create'
    * highlight "Raise Request" instead of "My Requests".

    * Why it's better: parent/child nav items never double-highlight, and deeper
    * routes without their own nav item (e.g. '/dashboard/assets/123') still keep
    * the closest parent ("All Assets") highlighted. New nested routes need no
    * extra flags, just add the item to navConfig.

    * Caveat: only one item can be active at a time, and matching is based on the
    * visible items, so a hidden (role-restricted) item never affects the result.
*/

function Sidebar({ sections, isMobileOpen, setIsMobileOpen }: SidebarProps) {
    const location = useLocation();
    const navigate = useNavigate();
    const queryClient = useQueryClient();

    const { data: user, isPending } = useAuth();

    const [isLoggingOut, setIsLoggingOut] = React.useState<boolean>(false);

    const visibleSections = React.useMemo(
        () =>
            sections.map((section) => ({
                    ...section,
                    items: section.items.filter((item) => !item.roles || (user && item.roles.includes(user.role))),
                }))
                .filter((section) => section.items.length > 0),
        [sections, user]
    );

    const activeHref = React.useMemo(
        () =>
            visibleSections
                .flatMap((s) => s.items)
                .filter((item) => matches(location.pathname, item.href))
                .sort((a, b) => b.href.length - a.href.length)[0]?.href,
        [visibleSections, location.pathname]
    );

    React.useEffect(() => {
        setIsMobileOpen(false);
    }, [location.pathname, setIsMobileOpen]);

    React.useEffect(() => {
        if (!isMobileOpen) return;

        const onKey = (e: KeyboardEvent) => e.key === 'Escape' && setIsMobileOpen(false);

        window.addEventListener('keydown', onKey);

        return () => window.removeEventListener('keydown', onKey);
    }, [isMobileOpen, setIsMobileOpen]);

    const handleLogout = async () => {
        if (isLoggingOut) return;
        setIsLoggingOut(true);

        try {
            await api.post('/auth/logout');

            queryClient.clear();

            toast.success('Logged out successfully');

            navigate('/', { replace: true });
        } catch {
            toast.error('Could not log out. Please try again.');
        } finally {
            setIsLoggingOut(false);
        }
    }

    return (
        <>
            <div onClick={() => setIsMobileOpen(false)} aria-hidden='true' className={`fixed inset-0 z-30 bg-neutral-900/40 backdrop-blur-sm transition-opacity duration-300 lg:hidden ${isMobileOpen ? 'opacity-100' : 'pointer-events-none opacity-0'}`} />

            <aside className={`fixed inset-y-0 left-0 z-40 flex w-72 flex-col border-r border-neutral-200 bg-background transition-transform duration-300 lg:w-64 lg:translate-x-0 ${isMobileOpen ? 'translate-x-0' : '-translate-x-full'}`}>
                <div className='flex items-center justify-between border-b border-neutral-200 px-6 py-4'>
                    <img src={logo} alt='EquipSync Logo' className='h-9 w-auto object-contain' />
                    <button type='button' onClick={() => setIsMobileOpen(false)} aria-label='close-menu' className='rounded-full p-2 text-neutral-400 transition-colors hover:bg-neutral-100 hover:text-neutral-700 lg:hidden'>
                        <X className='h-4 w-4' />
                    </button>
                </div>

                <nav className='flex-1 space-y-6 overflow-y-auto px-4 py-6'>
                    {isPending ? (
                        // skeleton while the current user loads, so items don't pop in
                        <div className='space-y-2'>
                            {[0, 1, 2, 3].map((i) => (
                                <div key={i} className='h-10 animate-pulse rounded-xl bg-neutral-100' />
                            ))}
                        </div>
                    ) : (
                        visibleSections.map((section, i) => (
                            <div key={section.title ?? i} className='space-y-1'>
                                {section.title && (
                                    <p className='mb-2 px-3 text-xs font-bold uppercase tracking-wider text-neutral-400'>
                                        {section.title}
                                    </p>
                                )}

                                {section.items.map(({ label, href, icon: Icon }) => {
                                    const active = href === activeHref;

                                    return (
                                        <Link
                                            key={href}
                                            to={href}
                                            aria-current={active ? 'page' : undefined}
                                            className={`group flex items-center gap-3 rounded-xl px-3 py-2.5 text-xs font-bold uppercase tracking-wider transition-all duration-200 ${
                                                active ? 'bg-primary text-background shadow-md' : 'text-primary hover:bg-neutral-100'
                                            }`}
                                        >
                                            <Icon className='h-4 w-4 shrink-0' />
                                            <span>{label}</span>
                                        </Link>
                                    );
                                })}
                            </div>
                        ))
                    )}
                </nav>

                <div className='space-y-3 border-t border-neutral-200 p-4'>
                    {user && (
                        <div className='px-3'>
                            <p className='truncate text-sm font-medium text-neutral-900'>{user.email}</p>
                            <p className='text-xs text-neutral-500'>{user.role}</p>
                        </div>
                    )}

                    <button type='button' onClick={handleLogout} disabled={isLoggingOut} className='flex w-full cursor-pointer items-center gap-3 rounded-xl px-3 py-2.5 text-xs font-bold uppercase tracking-wider text-neutral-500 transition-colors hover:bg-neutral-100 hover:text-neutral-900 disabled:cursor-not-allowed disabled:opacity-60'>
                        <LogOut className='h-4 w-4 shrink-0' />
                        {isLoggingOut ? 'Logging out...' : 'Logout'}
                    </button>
                </div>
            </aside>
        </>
    );
}

export default Sidebar;