import type { LucideIcon } from 'lucide-react';
import { ClipboardList, LayoutDashboard, Monitor, CheckCircle, GitPullRequestCreateArrow, ClipboardCheck, Boxes, Users } from 'lucide-react';

import type { AuthUser } from '@/types';

export type Role = AuthUser['role'];

export interface NavItem {
    label: string,
    href: string,
    icon: LucideIcon,
    roles?: Role[]
}

export interface NavSection {
    title?: string;
    items: NavItem[];
}

export const navSections: NavSection[] = [
    {
        items: [
            { label: 'Dashboard', href: '/dashboard', icon: LayoutDashboard },
            { label: 'All Assets', href: '/dashboard/assets', icon: Monitor, roles: ['Admin', 'Manager'] },
            { label: 'Available Assets', href: '/dashboard/assets/available', icon: CheckCircle },
            { label: 'My Requests', href: '/dashboard/requests', icon: ClipboardList },
            { label: 'Raise Request', href: '/dashboard/requests/create', icon: GitPullRequestCreateArrow },
        ],
    },
    {
        title: 'Manage',
        items: [
            { label: 'Review Requests', href: '/manage/requests', icon: ClipboardCheck, roles: ['Admin', 'Manager'] },
            { label: 'Manage Assets', href: '/manage/assets', icon: Boxes, roles: ['Admin', 'Manager'] },
            { label: 'Users', href: '/manage/users', icon: Users, roles: ['Admin'] }
        ],
    },
];