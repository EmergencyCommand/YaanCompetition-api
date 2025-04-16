import psycopg2
from neo4j import GraphDatabase
from typing import List, Dict, Tuple

# 数据库配置（实际使用时建议从环境变量读取）
PG_CONFIG = {
    "host": "远程PG的IP或域名",
    "port": 5432,
    "database": "your_db",
    "user": "your_user",
    "password": "your_password",
    "sslmode": "require"
}

NEO4J_CONFIG = {
    "uri": "bolt://远程Neo4j的IP或域名:7687",
    "auth": ("neo4j", "your_neo4j_password"),
    "encrypted": True
}

# 定义每个表的三元组生成规则
TABLE_RULES = {
    # 规则1：warehouse_info 表 → (救灾能力储备信息)-[关系]->(仓库名)
    "warehouse_info": {
        "query": "SELECT warehouse_name, warehouse_type FROM warehouse_info",
        "cypher_template": """
            MERGE (root:Entity {name: '救灾能力储备信息'})
            MERGE (a:Entity {name: $name})
            MERGE (root)-[:%s]->(a)
        """,
        "relation_cleaner": lambda rel: rel.replace("（", "").replace("）", "").replace(" ", "")
    },

    # 规则2：material_info 表 → (物资类型)-[属于]->(仓库名)
    "material_info": {
        "query": "SELECT material_name, warehouse_name FROM material_info",
        "cypher_template": """
            MERGE (type:MaterialType {name: $material_name})
            MERGE (warehouse:Entity {name: $warehouse_name})
            MERGE (type)-[:属于]->(warehouse)
        """,
        "relation_cleaner": None  # 无需处理关系类型
    }
}

class Neo4jImporter:
    def __init__(self):
        self.driver = GraphDatabase.driver(**NEO4J_CONFIG)

    def close(self):
        self.driver.close()

    def import_data(self, table_name: str, data: List[Tuple]):
        """根据表规则导入数据到 Neo4j"""
        rule = TABLE_RULES[table_name]
        with self.driver.session() as session:
            for row in data:
                # 动态生成 Cypher
                params = {}
                if table_name == "warehouse_info":
                    name, rel_type = row
                    clean_rel = rule["relation_cleaner"](rel_type)
                    cypher = rule["cypher_template"] % clean_rel
                    params = {"name": name}
                elif table_name == "material_info":
                    material_name, warehouse_name = row
                    cypher = rule["cypher_template"]
                    params = {"material_name": material_name, "warehouse_name": warehouse_name}

                # 执行 Cypher
                session.run(cypher, **params)

def fetch_data_from_pg(table_name: str) -> List[Tuple]:
    """从 PostgreSQL 指定表读取数据"""
    conn = psycopg2.connect(**PG_CONFIG)
    cursor = conn.cursor()
    query = TABLE_RULES[table_name]["query"]
    cursor.execute(query)
    rows = cursor.fetchall()
    cursor.close()
    conn.close()
    return rows

def main():
    importer = Neo4jImporter()
    try:
        # 按顺序处理所有表
        for table_name in TABLE_RULES.keys():
            print(f"正在处理表: {table_name}...")
            data = fetch_data_from_pg(table_name)
            importer.import_data(table_name, data)
            print(f"已导入 {len(data)} 条数据")
    finally:
        importer.close()

if __name__ == "__main__":
    main()