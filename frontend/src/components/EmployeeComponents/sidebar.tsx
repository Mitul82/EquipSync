import React from 'react';
import toast from 'react-hot-toast';
import { useQueryClient } from '@tanstack/react-query';
import { Link, useLocation, useNavigate } from 'react-router-dom';
import { ClipboardList, LayoutDashboard, LogOut, Monitor, X, CheckCircle, GitPullRequestCreateArrow } from 'lucide-react';

import api from '@/utils/api';
import logo from '@/assets/equipsync.svg';

interface SidebarProps {
    isMobileOpen: boolean,
    setIsMobileOpen: (open: boolean) => void
}

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

const navItems = [
    { label: 'Dashboard', href: '/dashboard', icon: LayoutDashboard, end: true },
    { label: 'All Assets', href: '/dashboard/assets', icon: Monitor, end: true },
    { label: 'Available Assets', href: '/dashboard/assets/available', icon: CheckCircle },
    { label: 'My Requests', href: '/dashboard/requests', icon: ClipboardList, end: true },
    { label: 'Raise Request', href: '/dashboard/requests/create', icon: GitPullRequestCreateArrow, end: true }
];

function Sidebar({ isMobileOpen, setIsMobileOpen }: SidebarProps) {
    const location = useLocation();
    const navigate = useNavigate();
    const queryClient = useQueryClient();

    const [isLoggingOut, setIsLoggingOut] = React.useState<boolean>(false);

    React.useEffect(() => {
        setIsMobileOpen(false);
    }, [location.pathname, setIsMobileOpen]);

    React.useEffect(() => {
        if (!isMobileOpen) return;

        const onKey = (e: KeyboardEvent) => e.key === 'Escape' && setIsMobileOpen(false);

        window.addEventListener('keydown', onKey);

        return () => window.removeEventListener('keydown', onKey);
    }, [isMobileOpen, setIsMobileOpen]);

    const isActive = (href: string, end?: boolean) => end ? location.pathname === href : location.pathname === href || location.pathname.startsWith(`${href}/`);

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
            <div onClick={() => setIsMobileOpen(false)} aria-hidden='true' className={`fixed inset-0 z-30 bg-neutral-900/40 backdrop-blur-sm transition-opacity duration-300 lg:hidden ${isMobileOpen ? 'opacity-100' : 'pointer-events-none opacity-0'}`}/>

            <aside className={`fixed inset-y-0 left-0 z-40 flex w-72 flex-col border-r border-neutral-200 bg-background transition-transform duration-300 lg:w-64 lg:translate-x-0 ${isMobileOpen ? 'translate-x-0' : '-translate-x-full'}`}>
                <div className='flex items-center justify-between border-b border-neutral-200 px-6 py-4'>
                    <img src={logo} alt='EquipSync Logo' className='h-9 w-auto object-contain'/>
                    <button type='button' onClick={() => setIsMobileOpen(false)} aria-label='close-menu' className='rounded-full p-2 text-neutral-400 transition-colors hover:bg-neutral-100 hover:text-neutral-700 lg:hidden'>
                        <X className='h-4 w-4' />
                    </button>
                </div>

                <nav className='flex-1 space-y-6 overflow-y-auto px-4 py-6'>
                    {navItems.map(({ label, href, icon: Icon, end }) => {
                        const active = isActive(href, end);

                        return (
                            <Link key={href} to={href} aria-current={active ? 'page' : undefined} className={`group flex items-center gap-3 rounded-xl px-3 py-2.5 text-xs font-bold uppercase tracking-wider transition-all duration-200 ${active ? 'bg-primary text-background shadow-md' : 'text-neutral-500 hover:bg-neutral-100 hover:text-neutral-900'}`}>
                                <Icon className={`h-4 w-4 shrink-0 ${active ? 'text-background' : 'text-primary'}`}/>
                                <span className={active ? 'text-background' : 'text-primary'}>{label}</span>
                            </Link>
                        );
                    })}

                    <div className='border-t border-neutral-200 p-4'>
                        <button type='button' onClick={handleLogout} disabled={isLoggingOut} className='flex w-full cursor-pointer items-center gap-3 rounded-xl px-3 py-2.5 text-xs font-bold uppercase tracking-wider text-neutral-500 transition-colors hover:bg-neutral-100 hover:text-neutral-900 disabled:cursor-not-allowed disabled:opacity-60'>
                            <LogOut className='h-4 w-4 shrink-0' />
                            {isLoggingOut ? 'Logging out...' : 'Logout'}
                        </button>
                    </div>
                </nav>
            </aside>
        </>
    );
}

export default Sidebar;