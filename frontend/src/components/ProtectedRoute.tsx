import React from 'react';
import { Navigate, Outlet, useLocation } from 'react-router-dom';

import useAuth from '@/hooks/useAuth';

interface ProtectedRouteProps {
    children?: React.ReactNode,
    allowedRoles?: Array<'Admin' | 'Employee' | 'Manager'>
}

function ProtectedRoute({ children, allowedRoles }: ProtectedRouteProps) {
    const location = useLocation();
    const { data: user, isLoading, isError } = useAuth();

    if(isLoading) {
        return (
            <div className='flex min-h-[60vh] items-center justify-center'>
                <span className='relative flex h-4 w-4'>
                    <span className='absolute inline-flex h-full w-full animate-ping rounded-full bg-primary opacity-75' />
                    <span className='relative inline-flex h-4 w-4 rounded-full bg-primary' />
                </span>
            </div>
        );
    }

    if(isError || !user) {
        return <Navigate to={`/?from=${location.pathname}`} replace={true} />;
    }

    if (allowedRoles && !allowedRoles.includes(user.role)) {
        const searchParams = new URLSearchParams(window.location.search);
        const redirectUrl = searchParams.get('from');

        if (redirectUrl) {
            return <Navigate to={decodeURIComponent(redirectUrl)}/>;
        } else if (user?.id) {
            return <Navigate to='/dashboard' replace={true}/>;
        } else {
            return <Navigate to='/'/>;
        }
    }

    return (
        <>
            { children ?? <Outlet/> }
        </>
    );
}

export default ProtectedRoute;