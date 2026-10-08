import { ArrowLeft, Home } from 'lucide-react';
import { Link, useNavigate } from 'react-router-dom';

import logo from '@/assets/equipsync.svg';

function NotFoundPage() {
    const navigate = useNavigate();

    return (
        <main className='relative flex min-h-screen flex-col bg-background'>
            <header className='relative z-20 flex w-full items-center border-b border-neutral-200 bg-background/95 px-6 py-4 backdrop-blur-md sm:px-12'>
                <img src={logo} alt='EquipSync Logo' className='h-9 w-auto object-contain' />
            </header>

            <div className='pointer-events-none absolute inset-0 opacity-10 bg-[linear-gradient(to_right,#1f1f1f_1px,transparent_1px),linear-gradient(to_bottom,#1f1f1f_1px,transparent_1px)] bg-size-[4rem_4rem]' />

            <div className='relative z-10 flex flex-1 items-center justify-center px-4 py-12'>
                <div className='w-full max-w-md rounded-3xl border border-neutral-200 bg-background p-8 text-center shadow-sm'>
                    <p className='text-7xl font-bold tracking-wider text-primary'>404</p>

                    <h1 className='mt-4 text-sm font-bold uppercase tracking-wider text-neutral-900'>Page not found</h1>
                    <p className='mt-2 text-xs text-neutral-500'>
                        The page you are looking for doesn't exist or may have been moved.
                    </p>

                    <div className='mt-8 flex flex-col-reverse gap-3 sm:flex-row'>
                        <button type='button' onClick={() => navigate(-1)} className='inline-flex flex-1 cursor-pointer items-center justify-center gap-2 rounded-full border border-neutral-200 py-3 text-xs font-bold uppercase tracking-widest text-neutral-600 transition-all hover:bg-neutral-100'>
                            <ArrowLeft className='h-4 w-4' />
                            Go Back
                        </button>
                        <Link to='/dashboard' className='inline-flex flex-1 items-center justify-center gap-2 rounded-full bg-primary py-3 text-xs font-bold uppercase tracking-widest text-background shadow-md transition-all hover:bg-primary/90 hover:shadow-lg'>
                            <Home className='h-4 w-4' />
                            Dashboard
                        </Link>
                    </div>
                </div>
            </div>
        </main>
    );
}

export default NotFoundPage;