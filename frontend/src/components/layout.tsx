import React from 'react';
import { Menu } from 'lucide-react';
import { Outlet } from 'react-router-dom';

import Sidebar from './sidebar';
import logo from '@/assets/equipsync.svg';
import { navSections } from './navConfig';

function Layout() {
    const [isMobileOpen, setIsMobileOpen] = React.useState(false);

    return (
        <div className='min-h-screen bg-background'>
            <Sidebar sections={navSections} isMobileOpen={isMobileOpen} setIsMobileOpen={setIsMobileOpen}/>

            <div className='relative min-h-screen lg:pl-64'>
                <header className='sticky top-0 z-20 flex items-center gap-3 border-b border-neutral-200 bg-background/95 px-4 py-3 backdrop-blur-md lg:hidden'>
                    <button type='button' onClick={() => setIsMobileOpen(true)} aria-label='Open menu' className='rounded-full p-2 text-neutral-600 transition-colors hover:bg-neutral-100'>
                        <Menu className='h-5 w-5' />
                    </button>
                    <img src={logo} alt='EquipSync Logo' className='h-7 w-auto object-contain' />
                </header>

                <div className='pointer-events-none absolute inset-0 z-0 opacity-10 bg-[linear-gradient(to_right,#1f1f1f_1px,transparent_1px),linear-gradient(to_bottom,#1f1f1f_1px,transparent_1px)] bg-size-[4rem_4rem]' />

                <main className='relative z-10 p-4 sm:p-8'>
                    <Outlet />
                </main>
            </div>
        </div>
    );
}

export default Layout;