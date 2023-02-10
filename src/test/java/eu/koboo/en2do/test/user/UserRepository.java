package eu.koboo.en2do.test.user;

import eu.koboo.en2do.repository.*;
import eu.koboo.en2do.repository.methods.async.Async;
import eu.koboo.en2do.repository.methods.fields.UpdateBatch;
import eu.koboo.en2do.repository.methods.pagination.Pagination;
import eu.koboo.en2do.repository.methods.sort.Limit;
import eu.koboo.en2do.repository.methods.sort.Skip;
import eu.koboo.en2do.repository.methods.sort.Sort;
import eu.koboo.en2do.repository.methods.sort.SortBy;
import eu.koboo.en2do.repository.methods.transform.Transform;
import eu.koboo.en2do.test.customer.Customer;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@SuppressWarnings("unused")
@Collection("user_repository")
public interface UserRepository extends Repository<User, UUID>, AsyncRepository<User, UUID> {

}
