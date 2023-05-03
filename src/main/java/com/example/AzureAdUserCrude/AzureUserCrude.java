package com.example.AzureAdUserCrude;

import com.azure.identity.ClientSecretCredential;
import com.azure.identity.ClientSecretCredentialBuilder;
import com.microsoft.graph.authentication.TokenCredentialAuthProvider;
import com.microsoft.graph.models.User;
import com.microsoft.graph.options.HeaderOption;
import com.microsoft.graph.options.Option;
import com.microsoft.graph.requests.GraphServiceClient;
import com.microsoft.graph.requests.UserCollectionPage;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;


public class AzureUserCrude {
    private String clientId;
    private String clientSecret;
    private String tenantId;

    public AzureUserCrude(String clientId, String clientSecret, String tenantId) {
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.tenantId = tenantId;
    }

    public User createUser(User user) {
        GraphServiceClient graphClient = graphServiceClient();
        User data = graphClient.users().buildRequest().post(user);
        return data;
    }


    public UserCollectionPage getUser(String query) {
        GraphServiceClient graphClient = graphServiceClient();
        LinkedList<Option> requestOptions = new LinkedList<Option>();
        requestOptions.add(new HeaderOption("ConsistencyLevel", "eventual"));
        UserCollectionPage users = graphClient.users().buildRequest(requestOptions)
                .filter(query).count().get();
        return users;
    }

    public List<User> delete(String query) {
        GraphServiceClient graphClient = graphServiceClient();
        LinkedList<Option> requestOptions = new LinkedList<Option>();
        requestOptions.add(new HeaderOption("ConsistencyLevel", "eventual"));
        UserCollectionPage userCollectionPage = graphClient.users().buildRequest(requestOptions)
                .filter(query).count().get();
        List<User> data = userCollectionPage.getCurrentPage();
        List<User> deletedUser = new ArrayList<>();
        if (!CollectionUtils.isEmpty(data)) {
            for (User user : data) {
                deletedUser.add(graphClient.users(user.id).buildRequest().delete());
            }
        }
        return deletedUser;
    }

   /* public PasswordResetResponse resetPassword() {
        GraphServiceClient graphClient = graphServiceClient();
        PasswordAuthenticationMethodCollectionPage passwordMethods =
                graphClient.users("773d42db-6e41-466c-9f36-64965f3ec671").authentication().passwordMethods()
                        .buildRequest().get();
        String newPassword = "Admin@123";
        PasswordResetResponse response =
                graphClient.users("773d42db-6e41-466c-9f36-64965f3ec671").authentication()
                        .methods(passwordMethods.getCurrentPage().get(0).id).resetPassword(
                                AuthenticationMethodResetPasswordParameterSet.newBuilder()
                                        .withNewPassword(newPassword).build()).buildRequest().post();

        return response;
    }*/

    private GraphServiceClient graphServiceClient() {
        final ClientSecretCredential clientSecretCredential =
                new ClientSecretCredentialBuilder().clientId(clientId)
                        .clientSecret(clientSecret)
                        .tenantId(tenantId).build();

        final TokenCredentialAuthProvider tokenCredentialAuthProvider =
                new TokenCredentialAuthProvider(clientSecretCredential);

        GraphServiceClient graphClient =
                GraphServiceClient.builder().authenticationProvider(tokenCredentialAuthProvider)
                        .buildClient();

        return graphClient;
    }
}
