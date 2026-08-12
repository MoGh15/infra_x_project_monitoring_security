package mohammad.development.praxis.modules.admin;

import lombok.RequiredArgsConstructor;
import mohammad.development.praxis.repos.AdminRepository;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdminUserDetailsService implements UserDetailsService {

    private final AdminRepository adminRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Admin admin = adminRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Admin not found"));

        return User.builder()
                .username(admin.getUsername())
                .password(admin.getPasswordHash())
                .disabled(!admin.isEnabled())
                .roles(admin.getRoles().toArray(new String[0])) // z.B. ["ADMIN"]
                .build();
    }
}