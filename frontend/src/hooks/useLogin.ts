import { toast } from 'react-hot-toast';
import { useNavigate } from 'react-router-dom';
import { useMutation } from '@tanstack/react-query';

import api from '@/utils/api.ts';

import type { AxiosError } from 'axios';
import type { ApiRes } from '@/types/ApiResponse.ts';

type Credentials = {
    email: string,
    password: string
}

type AuthUser = {
    id: string,
    email: string,
    department: string,
    role: 'Admin' | 'Manager' | 'Employee'
}

function useLogin() {
    const navigate = useNavigate();

    return useMutation({
        mutationFn: async (credentials: Credentials) => {
            const { data }: { data: ApiRes<AuthUser> } = await api.post('/auth/login', credentials);

            return data;
        },
        onMutate: () => {
            toast.loading('Logging in...', { id: 'login-toast' });
        },
        onSuccess: (data: ApiRes<AuthUser>) => {
            toast.success(data.message || 'Logged in', { id:  'login-toast' });

            const searchParams = new URLSearchParams(window.location.search);
            const redirectUrl = searchParams.get('from');

            if (redirectUrl) {
            	navigate(decodeURIComponent(redirectUrl));
            } else if (data?.data?.role === 'Admin') {
            	navigate('/admin');
            } else if (data?.data?.role === 'Manager') {
              	navigate('/manager');
            } else if (data?.data?.role === 'Employee') {
            	navigate('/dashboard');
            } else {
            	navigate('/');
            }
        },
        onError: (err: AxiosError<{ message?: string }>) => {
            const errMessage = err.response?.data?.message || 'An Error Occured';
            
            toast.error(errMessage, { id: 'login-toast' });
        }
    });
}

export default useLogin;