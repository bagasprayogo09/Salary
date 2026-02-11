package com.salary.backend_salary.entity.export;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QExport is a Querydsl query type for Export
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QExport extends EntityPathBase<ExportJob> {

    private static final long serialVersionUID = -927919259L;

    public static final QExport export = new QExport("export");

    public final DateTimePath<java.time.LocalDateTime> createdAt = createDateTime("createdAt", java.time.LocalDateTime.class);

    public final StringPath downloadUrl = createString("downloadUrl");

    public final StringPath errorMessage = createString("errorMessage");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final NumberPath<Integer> progress = createNumber("progress", Integer.class);

    public final StringPath status = createString("status");

    public QExport(String variable) {
        super(ExportJob.class, forVariable(variable));
    }

    public QExport(Path<? extends ExportJob> path) {
        super(path.getType(), path.getMetadata());
    }

    public QExport(PathMetadata metadata) {
        super(ExportJob.class, metadata);
    }

}

