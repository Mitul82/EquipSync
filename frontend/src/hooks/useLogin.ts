import { toast } from 'react-hot-toast';
import { useNavigate } from 'react-router-dom';
import { useMutation } from '@tanstack/react-query';

import api from '@/utils/api.ts';

import type { ApiRes } from '@/types/ApiResponse.ts';

type Credentials = {
    email: string,
    password: string
}

function useLogin() {
    const navigate = useNavigate();

    return useMutation({
        mutationFn: async (credentials: Credentials) => {
            const { data }: { data: ApiRes } = await api.post('/auth/login', credentials);

            return data;
        },
        onMutate: () => {
            toast.loading('Logging in...', { id: 'login-toast' });
        },
        onSuccess: (data: ApiRes) => {
            toast.success(data.message || 'Logged in', { id:  'login-toast' });

            const searchParams = new URLSearchParams(window.location.search);
            const redirectUrl = searchParams.get('from');

            if (redirectUrl) {
            	navigate(decodeURIComponent(redirectUrl));
            } else if (data.user?.role === 'Admin') {
            	navigate('/admin');
            } else if (data.user?.role === 'Manager') {
              	navigate('/manager');
            } else if (data.user?.role === 'Employee') {
            	navigate('/dashboard');
            } else {
            	navigate('/');
            }
        },
        onError: (err: any) => {
            const errMessage = err.response?.data?.message || 'An Error Occured';
            
            toast.error(errMessage, { id: 'login-toast' });
        }
    });
}

export default useLogin;